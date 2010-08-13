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
		eachObject: function(iterator){
			this.state.eachObject(iterator);
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



