var PartyProp = JunctionProps.Prop.extend(
	{
		init: function(propName){
			this._super(propName, new this.PartyPropState(null),
						propName + "_" + randomUUID());
		},
		reifyState: function(jsonObj){
			return new this.PartyPropState(jsonObj);
		},
		addObject: function(item){
			this.addOperation({type:"addObj", item:item});
		},

		addPicture: function(userId,url,thumbUrl,caption){
			this.addObject(
				{
					id: randomUUID(),
					type: "image",
					url: url,
					thumbUrl: thumbUrl,
					time: (new Date()).getTime(),
					caption: caption,
					owner: userId
				});
		},

		addYoutube: function(userId,videoId,thumbUrl,caption){
			this.addObject(
				{
					id: randomUUID(),
					type: "youtube",
					videoId: videoId,
					thumbUrl: thumbUrl,
					time: (new Date()).getTime(),
					caption: caption,
					owner: userId
				});
		},

		eachObject: function(iterator){
			this.state.eachObject(iterator);
		},
		getPictures: function(){
			return this.state.getPictures();
		},
		getPlaylist: function(){
			return this.state.getPlaylist();
		},

		PartyPropState: Class.extend(
			{
				init: function(jsonObj){
					if(jsonObj == null){
						this.raw = {
							objects: {},
							timeline: []
						};
					}
					else{
						this.raw = jsonObj;	
					}
				},

				applyOperation: function(op){
					if(op.type == "addObj"){
						var obj = op.item;
						this.raw.objects[obj.id] = obj;
					}
					else if(op.type == "addToTimeline"){
						this.raw.timeline.push(op.itemId);
					}
				},

				eachObject: function(iterator){
					for(var id in this.raw.objects){
						var obj = this.raw.objects[id];
						iterator(obj);
					}
				},

				getPictures: function(){
					var pics = [];
					for(var id in this.raw.objects){
						var obj = this.raw.objects[id];
						if(obj.type == "image"){
							pics.push(obj);
						}
					}
					pics.sort(function(a,b){ return b.time - a.time; });
					return pics;
				},

				getPlaylist: function(){
					var vids = [];
					for(var id in this.raw.objects){
						var obj = this.raw.objects[id];
						if(obj.type == "youtube"){
							vids.push(obj);
						}
					}
					vids.sort(function(a,b){ return b.time - a.time; });
					return vids;
				},


				toJSON:function(){
					return JSON.parse(JSON.stringify(this.raw));
				},

				hashCode: function(){
					return 1;
				},

				copy: function(){
					return new PartyProp.prototype.PartyPropState(this.toJSON());
				}
			})

	});



