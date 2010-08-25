var PartyProp = JunctionProps.Prop.extend(
	{
		init: function(propName){
			var self = this;
			this._super(propName, new this.PartyPropState(null),
						propName + "_" + randomUUID());

			this.topVideoChangedListeners = [];
			this.currentTopVideo = null;

			this.addChangeListener({ type: "change",
									 onChange: function(o){
										 self.updateOnChange();
									 }});
			this.addChangeListener({ type: "sync",
									 onChange: function(o){
										 self.updateOnChange();
									 }});
		},
		
		/** 
		 * Notify app of playlist change.
		 */
		updateOnChange: function(){
			var pl = this.getPlaylist();
			if(pl.length > 0){
				var top = pl[0];
				if(this.currentTopVideo == null || top.id != this.currentTopVideo.id){
					this.currentTopVideo = top;
					for(var i = 0; i < this.topVideoChangedListeners.length; i++){
						(this.topVideoChangedListeners[i])(this.currentTopVideo);
					}
				}
			}
			else {
				this.currentTopVideo = null;
			}
		},

		/** 
		 * Register a listener for this app-level event.
		 */
		addTopVideoChangedListener: function(func){
			this.topVideoChangedListeners.push(func);
		},


		reifyState: function(jsonObj){
			return new this.PartyPropState(jsonObj);
		},

		getName: function(){
			return this.state.getName();
		},

		setName: function(name){
			this.addOperation({type:"setName", name: name});
		},

		addObject: function(item){
			this.addOperation({type:"addObj", item:item});
		},

		deleteObject: function(item){
			this.addOperation({type:"deleteObj", itemId: item.id});
		},

		addPicture: function(userId,url,thumbUrl,caption){
			this.addObject(
				{
					id: randomUUID(),
					type: "image",
					url: url,
					thumbUrl: thumbUrl,
					time: Math.ceil((new Date()).getTime() / 1000.0),
					caption: caption,
					owner: userId
				});
		},

		deleteTopVideo: function(){ 
			if(this.currentTopVideo != null){
				this.deleteObject(this.currentTopVideo);
			}
		},

		addYoutube: function(userId,videoId,thumbUrl,caption){
			this.addObject(
				{
					id: randomUUID(),
					type: "youtube",
					videoId: videoId,
					thumbUrl: thumbUrl,
					time: Math.ceil((new Date()).getTime() / 1000),
					caption: caption,
					owner: userId,
					votes: 0
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
							name: "Unnamed Party",
							objects: {}
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
					else if(op.type == "deleteObj"){
						var id = op.itemId;
						delete this.raw.objects[id];
					}
					else if(op.type == "setName"){
						this.raw.name = op.name;
					}
					else if(op.type == "vote"){
						var count = op.count;
						var obj = this.raw.objects[op.itemId];
						if(obj){
							var cur = obj.votes || 0;
							obj.votes = cur + count;
						}
					}
				},

				eachObject: function(iterator){
					for(var id in this.raw.objects){
						var obj = this.raw.objects[id];
						iterator(obj);
					}
				},

				getName: function(){ return this.raw.name; },

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
					vids.sort(function(a,b){ 
								  if(a.votes == b.votes) return (a.time - b.time);
								  else return b.votes - a.votes;
							  });
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



