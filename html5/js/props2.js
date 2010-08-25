var JunctionProps = new (
	function(){

		this.scr = function(){
			throw new Error("ERROR: Subclass responsibility!");
		};

		this.nyi = function(){
			throw new Error("ERROR: Not yet implemented!");
		};

		this.Prop = JX.JunctionExtra.extend(
			{
				init: function(propName, state, propReplicaName){
					this._super();

					this.MODE_NORM = 1;
					this.MODE_SYNC = 2;

					this.MSG_STATE_OPERATION = 1;
					this.MSG_WHO_HAS_STATE = 2;
					this.MSG_I_HAVE_STATE = 3;
					this.MSG_SEND_ME_STATE = 4;
					this.MSG_STATE_SYNC = 5;
					this.MSG_HELLO = 6;

					this.EVT_CHANGE = "change";
					this.EVT_SYNC = "sync";
					this.EVT_ANY = "*";

					this.NO_SEQ_NUM = -1;

					this.uuid = randomUUID().toString();
					this.propName = propName;
					this.propReplicaName = propReplicaName || propName + "-replica" + randomUUID();

					this.state = state;
					this.cleanState = state.copy();

					this.sequenceNum = 0;
					this.lastOpUUID = "";

					this.staleness = 0;

					this.mode = this.MODE_NORM;
					this.syncId = "";
					this.waitingForIHaveState = false;

					this.opsSYNC = [];
					this.pendingLocals = [];
					this.changeListeners = [];

					this.timeOfLastSyncRequest = 0;
					this.timeOfLastHello = 0;
					this.active = false;

					this.WAKEUP_INTERVAL = 1000;

					var self = this;
					setInterval(
						function(){self.periodicTask();}, 
						this.WAKEUP_INTERVAL);
				},


				periodicTask: function(){
					var t = (new Date()).getTime();
					if(this.active && this.actor != null &&
					   // should be null if actor has 'left' the activity
					   this.actor.junction != null 
					  ){
						  if(this.mode == this.MODE_NORM){
							  if((t - this.timeOfLastHello) > 3000){
								  this.sendHello();
							  }
						  }
						  else if(this.mode == this.MODE_SYNC){
							  if((t - this.timeOfLastSyncRequest) > 5000){
								  this.broadcastSyncRequest();
							  }
						  }
					  }
				},


				getStaleness: function(){
					return this.staleness;
				},


				getSequenceNum: function(){
					return this.sequenceNum;
				},

				getState: function(){
					return this.state;
				},

				stateToString: function(){
					return this.state.toString();
				},

				getPropName: function(){
					return this.propName;
				},

				logInfo: function(s){
					this.actor.junction.logInfo("prop@" + this.propReplicaName + ": " + s);
				},

				logErr: function(s){
					this.actor.junction.logError("prop@" + this.propReplicaName + ": " + s);
				},

				logState: function(s){
					this.actor.junction.logInfo("\n");
					this.logInfo(s);
					this.actor.junction.logInfo("pendingLocals: " + this.pendingLocals);
					this.actor.junction.logInfo("opsSync: " + this.opsSYNC);
					this.actor.junction.logInfo("sequenceNum: " + this.sequenceNum);
					this.actor.junction.logInfo("\n");
					this.actor.junction.logInfo("");
				},

				assertTrue: function(s, cond){
					if(!cond){
						this.logErr("ASSERTION FAILED: " + s);
					}
				},


				/*abstract*/ reifyState: function(jsonObj){ this.scr(); },


				addChangeListener: function(listener){
					this.changeListeners.push(listener);
				},

				/**
				 * Dispatch a change event to all listeners. Each listener will
				 * of type evtType will be applied to the argument o (an arbitrary
				 *  data value).
				 */
				dispatchChangeNotification: function(evtType, o){
					for(var i = 0; i < this.changeListeners.length; i++){
						var l = this.changeListeners[i];
						if(l.type == evtType || l.type == this.EVT_ANY){
							l.onChange(o);
						}						 
					}
				},

				/**
				 * Returns true if the normal event handling should proceed;
				 * Return false to stop cascading.
				 */
				beforeOnMessageReceived: function(msgHeader, jsonMsg) {
					if(jsonMsg.propTarget == this.propName){
						jsonMsg.senderActor = msgHeader.from;
						this.handleMessage(jsonMsg);
						return false;
					}
					else{
						return true; 
					}
				},


				/**
				 * What to do with a newly arrived operation? Depends on mode of 
				 * operation.
				 */
				handleReceivedOp: function(opMsg){
					var i;
					var msg;
					var changed = false;
					if(this.isSelfMsg(opMsg)){
						this.staleness = this.sequenceNum - opMsg.localSeqNum;
						this.cleanState.applyOperation(opMsg.op);

						var foundIndex = -1;
						var uuid = opMsg.uuid;
						for(i = 0; i < this.pendingLocals.length; i++){
							msg = this.pendingLocals[i];
							if(msg.uuid == uuid){
								foundIndex = i;
								break;
							}
						}
						if(foundIndex > -1){
							this.pendingLocals.splice(foundIndex, 1);
						}
					}
					else{
						if(this.pendingLocals.length > 0){
							this.cleanState.applyOperation(opMsg.op);
							this.state = this.cleanState.copy();
							for(i = 0; i < this.pendingLocals.length; i++){
								msg = this.pendingLocals[i];
								this.state.applyOperation(msg.op);
							}
						}
						else{
							this.assertTrue("If pending locals is empty, state hash and cleanState hash should be equal.", 
											this.state.hashCode() == this.cleanState.hashCode());
							var op = opMsg.op;
							this.cleanState.applyOperation(op);
							this.state.applyOperation(deepObjCopy(op));
						}
						changed = true;
					}

					this.lastOpUUID = opMsg.uuid;
					this.sequenceNum += 1;

					if(changed){
						this.dispatchChangeNotification(this.EVT_CHANGE, null);
					}

					this.logState("Got op off wire, finished processing: " + opMsg);
				},


				exitSYNCMode: function(){
					this.logInfo("Exiting SYNC mode");
					this.mode = this.MODE_NORM;
					this.syncId = "";
					this.waitingForIHaveState = false;
				},

				enterSYNCMode: function(desiredSeqNumber){
					this.logInfo("Entering SYNC mode.");
					this.mode = this.MODE_SYNC;
					this.syncId = randomUUID();
					this.sequenceNum = -1;
					clearArray(this.opsSYNC);
					this.waitingForIHaveState = true;
					this.broadcastSyncRequest();
				},

				broadcastSyncRequest: function(){
					this.timeOfLastSyncRequest = (new Date()).getTime();
					this.sendMessageToProp(this.newWhoHasStateMsg(this.syncId));
				},

				isSelfMsg: function(msg){
					return msg.senderReplicaUUID == this.uuid;
				},

				handleMessage: function(msg){
					var msgType = msg.type;
					var fromActor = msg.senderActor;
					switch(this.mode){
					case this.MODE_NORM:
						switch(msgType){
						case this.MSG_STATE_OPERATION: {
							this.handleReceivedOp(msg);
							break;
						}
						case this.MSG_SEND_ME_STATE:{
							if(!this.isSelfMsg(msg)){
								this.logInfo("Got SEND_ME_STATE");
								var syncId = msg.syncId;
								this.sendMessageToPropReplica(
									fromActor, 
									this.newStateSyncMsg(syncId));
							}
							break;
						}
						case this.MSG_HELLO:{
							if(this.isSelfMsg(msg)){
								this.logInfo("Got self HELLO.");
							}
							else{
								this.logInfo("Got peer HELLO.");
								if(msg.localSeqNum > this.sequenceNum) {
									this.enterSYNCMode();
								}
							}

							break;
						}
						case this.MSG_WHO_HAS_STATE: {
							if(!this.isSelfMsg(msg)){
								this.logInfo("Got WHO_HAS_STATE.");
								var syncId = msg.syncId;
								this.sendMessageToPropReplica(
									fromActor, this.newIHaveStateMsg(syncId));
							}
							break;
						}
						default:
							this.logInfo("NORM mode: Ignoring message, "  + msg);
						}
						break;
					case this.MODE_SYNC:
						switch(msgType){
						case this.MSG_STATE_OPERATION:{
							this.opsSYNC.push(msg);
							this.logInfo("SYNC mode: buffering op..");
							break;
						}
						case this.MSG_I_HAVE_STATE:{
							var syncId = msg.syncId;
							if(!this.isSelfMsg(msg) && 
							   this.waitingForIHaveState && syncId == this.syncId){
								this.logInfo("Got I_HAVE_STATE.");
								this.sendMessageToPropReplica(
									fromActor, 
									this.newSendMeStateMsg(syncId));
								this.waitingForIHaveState = false;
							}
							break;
						}
						case this.MSG_STATE_SYNC:{
							if(!this.isSelfMsg(msg)){
								// First check that this sync message 
								// corresponds to the current
								// SYNC mode...
								var syncId = msg.syncId;
								if(!(syncId == this.syncId)){
									this.logErr("Bogus sync id! ignoring StateSyncMsg");
								}
								else{
									this.handleStateSyncMsg(msg);
								}
							}
							break;
						}
						default:{
							this.logInfo("SYNC mode: Ignoring message, "  + msg);
						}
						}
					}
				},

				/**
				 * Install state received from peer.
				 */
				handleStateSyncMsg: function(msg){
					this.logInfo("Got StateSyncMsg:" + msg);

					this.logInfo("Reifying received state..");
					this.cleanState = this.reifyState(msg.state);
					this.logInfo("Copying clean to predicted..");
					this.state = this.cleanState.copy();
					this.sequenceNum = msg.seqNum;
					this.lastOpUUID = msg.lastOpUUID;

					this.logInfo("Installed state.");
					this.logInfo("sequenceNum:" + this.sequenceNum);
					this.logInfo("Now applying buffered things....");

					// Forget all local predictions.
					clearArray(this.pendingLocals);

					// Apply any ops that we recieved while syncing,
					// ignoring those that are already incorporated 
					// into sync state.
					var apply = false;
					var i = 0;
					for(i = 0; i < this.opsSYNC; i++){
						var m = this.opsSYNC[i];
						if(!apply && m.uuid == this.lastOpUUID){
							apply = true;
							continue;
						}
						else if(apply){
							this.handleReceivedOp(m);
						}
					}
					clearArray(this.opsSYNC);
					this.exitSYNCMode();
					this.logState("Finished syncing.");
					this.dispatchChangeNotification(this.EVT_SYNC, null);
				},


				/**
				 * Add an operation to the state managed by this Prop
				 */
				addOperation: function(operation){
					if(this.mode == this.MODE_NORM){
						this.logInfo("Adding predicted operation.");
						var msg = this.newStateOperationMsg(operation);
						this.state.applyOperation(operation);
						this.dispatchChangeNotification(this.EVT_CHANGE, operation);
						this.pendingLocals.push(msg);
						this.sendMessageToProp(msg);
					}
				},

				sendHello: function(){
					this.timeOfLastHello = (new Date()).getTime();
					this.sendMessageToProp(this.newHelloMsg());
				},


				/**
				 * Send a message to all prop-replicas in this prop
				 */
				sendMessageToProp: function(m){
					m.propTarget = this.propName;
					m.senderReplicaUUID = this.uuid;
					this.actor.sendMessageToSession(m);
				},


				/**
				 * Send a message to the prop-replica hosted at the given actorId.
				 */
				sendMessageToPropReplica: function(actorId, m){
					m.propTarget = this.propName;
					m.senderReplicaUUID = this.uuid;
					this.actor.sendMessageToActor(actorId, m);
				},
				
				afterActivityJoin: function() {
					this.active = true;
				},

				newHelloMsg: function(){
					var m = {
						type: this.MSG_HELLO,
						localSeqNum: this.sequenceNum
					};
					return m;
				},


				newIHaveStateMsg: function(syncId){
					var m = {
						type: this.MSG_I_HAVE_STATE,
						syncId: syncId
					};
					return m;
				},

				newWhoHasStateMsg: function(syncId){
					var m = {
						type: this.MSG_WHO_HAS_STATE,
						syncId: syncId
					};
					return m;
				},

				newStateOperationMsg: function(op){
					var m = {
						type: this.MSG_STATE_OPERATION,
						op: op,
						localSeqNum: this.sequenceNum,
						uuid: randomUUID()
					};
					return m;
				},


				newStateSyncMsg: function(syncId){
					var m = {
						type: this.MSG_STATE_SYNC,
						state: this.cleanState.toJSON(),
						seqNum: this.sequenceNum,
						lastOpUUID: this.lastOpUUID,
						syncId: syncId
					};
					return m;
				},

				newSendMeStateMsg: function(syncId){
					var m = {
						type: this.MSG_SEND_ME_STATE,
						syncId: syncId
					};
					return m;
				}

			});



		this.ListProp = this.Prop.extend(
			{

				init: function(propName){
					this._super(propName, new this.ListState({items:[]}), null);
				},

				add: function(item){
					this.addOperation({type:"addOp", item:item});
				},

				remove: function(item){
					this.addOperation({type:"deleteOp", item:item});
				},

				replace: function(item1, item2){
					this.addOperation({type:"replaceOp", item1:item1, item2:item2});
				},

				clear: function(){
					this.addOperation({type:"clearOp"});
				},

				eachItem: function(iter){
					this.state.eachItem(iter);
				},

				reifyState: function(jsonObj){
					return new this.ListState(jsonObj);
				},

				ListState: Class.extend(
					{
						init: function(jsonObj){
							var inItems = jsonObj.items;
							this.items = [];
							for(var i = 0; i < inItems.length; i++){
								this.items.push(inItems[i]);
							}
						},

						applyOperation: function(op){
							if(op.type == "addOp"){
								this.add(op.item);
							}
							else if(op.type == "deleteOp"){
								this.remove(op.item);
							}
							else if(op.type == "replaceOp"){
								this.replace(op.item1, op.item2);
							}
							else if(op.type == "clearOp"){
								this.clear();
							}
						},

						eachItem: function(iterator){
							for(var i = 0; i < this.items.length; i++){
								iterator(this.items[i]);
							}
						},

						toJSON: function(){
							var obj = {
								items: this.items
							};
							return obj;
						},

						hashCode: function(){
							var code = 0;
							for(var i = 0; i < this.items.length; i++){
								code ^= this.items[i].id;
							}
							return code;
						},

						copy: function(){
							return new (JunctionProps.
										ListProp.prototype.
										ListState)(this.toJSON());
						},

						add: function(item){
							this.items.push(item);
						},

						remove: function(item){
							var index = -1;
							for(var i = 0; i < this.items.length; i++){
								var ea = this.items[i];
								if(ea.id == item1.id){
									index = i;
									break;
								}
							}
							if(index > -1){
								this.items.splice(index, 1);
							}
						},

						replace: function(item1, item2){
							var index = -1;
							for(var i = 0; i < this.items.length; i++){
								var ea = this.items[i];
								if(ea.id == item1.id){
									index = i;
									break;
								}
							}
							if(index > -1){
								this.items.splice(index, 1, item2);
							}
						},

						clear: function(){
							clearArray(this.items);
						}

					})

			});




	})(); // end JunctionProps