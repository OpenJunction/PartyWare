var PartyWare =
	new (function(){


			 this.Party = Class.extend(
				 {
					 init: function(model){
						 var self = this;
						 this.model = model;

						 this.model.addChangeListener({ type: "change",
														onChange: function(o){
															self.modelChanged();
														}});

						 this.model.addChangeListener({ type: "sync",
														onChange: function(o){
															self.modelChanged();
														}});
					 },

					 modelChanged: function(){
						 var self = this;
						 $("#timeline").children.remove();
						 this.model.eachItemInTimeline(
							 function(item){
								 self.appendItem(item);
							 });
					 },

					 appendItem: function(item){
						 if(item.type == "image"){
							 $("#timeline").append(
								 $('<img/>').attr(
									 {
										 src: item.url
									 }));

						 }
						 else if(item.type == "youtube"){
							 
						 }
					 }

				 });


			 this.init = function(){

				 var model = new PartyProp("party_prop");
				 var party = null;

				 var actor = {
					 roles: ["participant"],
					 onMessageReceived: function(msg, header) {
						 if(msg.text){
							 alert(msg.text);
						 }
						 else{
							 alert("Unexpected message: " + JSON.stringify(msg));
						 }
					 },
					 onActivityJoin: function() {
						 board = new PartyWare.Party(model);
					 },
					 initialExtras: [model]
				 };


				 var ascript = {
					 host: "openjunction.org",
					 ad: "edu.stanford.junction.partyware",
					 friendlyName: "PartyWare",
					 roles: { "buddy": {"platforms" : { /* platform definitions */ }}},
					 sessionID: "partyware"
				 };

				 var jx = JX.newJunction(ascript, actor);

				 $("#permalink").attr('href', jx.getInvitationForWeb("participant"));
			 };


		 })();
