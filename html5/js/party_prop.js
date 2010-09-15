var PartyProp = JunctionProps.Prop.extend(
	{
		init: function(propName){
			var self = this;
			this._super(propName, new this.PartyPropState(null),
						propName + "_" + randomUUID());

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
		},

		/** 
		 * Register a listener for this app-level event.
		 */
		addTopVideoChangedListener: function(func){
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

		resetVideoVotes: function(id){ 
			var vid = this.state.objectById(id);
			if(v){
				this.addYoutube(v.id, v.owner, v.videoId, v.thumbUrl, v.caption);
			}
		},

		resetVoteHistory: function(){ 
			this.addOperation({type:"resetVoteHistory"});
		},

		addYoutube: function(id, userId,videoId,thumbUrl,caption){
			this.addObject(
				{
					id: id || randomUUID(),
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
		getUsers: function(){
			return this.state.getUsers();
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
					else if(op.type == "resetVoteHistory"){
						// no-op on js version
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

				objectsOfType: function(tpe){
					var objs = [];
					for(var id in this.raw.objects){
						var obj = this.raw.objects[id];
						if(obj.type == tpe){
							objs.push(obj);
						}
					}
					return objs;
				},

				getPictures: function(){
					var pics = this.objectsOfType("image");
					pics.sort(function(a,b){ return b.time - a.time; });
					return pics;
				},

				getUsers: function(){
					var users = this.objectsOfType("user");
					return users;
				},

				getPlaylist: function(){
					var vids = this.objectsOfType("youtube");
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



