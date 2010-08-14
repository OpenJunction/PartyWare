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

						 $("#picInputButton").click(
							 function(){
								 var url = $("#picInput").val();
								 self.model.addPicture(self.userId, url, url, "..from web..");
							 });

						 $("#youtubeInputButton").click(
							 function(){
								 var videoId = $("#youtubeInput").val();
								 self.model.addYoutube(
									 self.userId, videoId,
									 "http://blogs.wyomingnews.com/blogs/backstagepass/files/2009/09/youtube_logo.jpg", 
									 "..from web..");
							 });
					 },

					 userId: randomUUID(),

					 modelChanged: function(){
						 var self = this;
						 var i,div;
						 $("#pictures").children().remove();
						 var pics = this.model.getPictures();
						 var table = this.buildTable(
							 pics, 5, 
							 function(ea,cell){
								 div = $("<div/>");
								 var img = $('<img/>').attr({src: ea.url});
								 $(div).append(img);
								 $(div).append($('<p/>').text("\'" + ea.caption + "\'"));
								 $(img).click(
									 function(){
										 Shadowbox.open(
											 {
												 player: "img",
												 content: ea.url
											 });
									 });

								 $(cell).append(div);
							 });
						 $("#pictures").append(table);

						 $("#playlist").children().remove();
						 var vids = this.model.getPlaylist();
						 for(i = 0; i < vids.length; i++){
							 var ea = vids[i];
							 div = $("<div/>").addClass("youtubeItem");
							 var img = $('<img/>').attr({src: ea.thumbUrl});
							 $(div).append(img);
							 $(div).append($('<p/>').text("\'" + ea.caption + "\'"));
							 $(img).click(
								 function(){
									 loadNewVideo(ea.videoId, 0);
								 });
							 $("#playlist").append(div);
						 }
					 },

					 buildTable: function(items, numCols, iter){
						 var table = $("<table/>");
						 var numRows = Math.ceil(items.length / numCols);
						 for(var r = 0; r < numRows; r++){
							 var row = $("<tr/>");
							 var c = 0;
							 for(c = 0; c < numCols; c++){
								 var index = numCols * r + c;
								 if(index >= items.length){
									 break;
								 }
								 var cell = $("<td/>");
								 $(row).append(cell);
								 iter(items[index], cell);
							 }
							 if(c > 0){
								 $(table).append(row);
							 }
						 }
						 return table;
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
