var PartyProp = JunctionProps.Prop.extend(
	{
		init: function(propName){
			this._super(propName, new this.PartyPropState({}),
						propName + "_" + randomUUID());
		},
		reifyState: function(jsonObj){
			return new this.ListState(jsonObj);
		},
		addObject: function(item){
			this.addOperation({type:"addObj", item:item});
		},
		eachItemInTimeline: function(iterator){
			this.state.eachItemInTimeline(iterator);
		},

		PartyPropState: Class.extend(
			{
				init: function(jsonObj){
					this.raw = jsonObj;
				},

				applyOperation: function(op){
					if(op.type == "addObj"){
						var obj = op.obj;
						this.raw.objects[obj.id] = obj;
					}
					else if(op.type == "addToTimeline"){
						this.raw.timeline.push(op.objId);
					}
				},

				eachItemInTimeline: function(iterator){
					var timeline = this.raw.timeline;
					var len = timeline.length;
					for(var i=0; i < len; i++){
						var id = timeline[i];
						var obj = this.objects[id];
						iterator(obj);
					}
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



