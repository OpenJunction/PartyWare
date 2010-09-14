var PartyWare =
	new (function(){


			 this.Party = Class.extend(
				 {
					 init: function(model){
						 var self = this;
						 this.model = model;

						 this.model.addChangeListener({ type: "*",
														onChange: function(o){
															self.modelChanged();
														}});

						 this.model.addChangeListener({ type: "sync",
														onChange: function(o){
															// Kick off the playlist
															var nextVid = self.model.topVotedVideo();
															if(nextVid){
																loadNewVideo(nextVid.videoId, 0);
															}
														}});

						 $("#nameInputButton").click(
							 function(){
								 var name = $("#nameInput").val();
								 self.model.setName(name);
							 });


						 $("#picInputButton").click(
							 function(){
								 var url = $("#picInput").val();
								 self.model.addPicture(self.userId, url, url, "..from web..");
							 });

						 $("#youtubeInputButton").click(
							 function(){
								 var videoId = $("#youtubeInput").val();
								 self.model.addYoutube(
									 null, self.userId, videoId,
									 "http://blogs.wyomingnews.com/blogs/backstagepass/files/2009/09/youtube_logo.jpg", 
									 "..from web..");
							 });

						 $("#nextVideoButton").click(
							 function(){
								 gotoNextVideo();
							 });
					 },

					 userId: randomUUID(),

					 modelChanged: function(){
						 var self = this;
						 var i,div;

						 // Update the pictures list 

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

						 // Update the youtube list

						 $("#playlist").children().remove();
						 var vids = this.model.getPlaylist();
						 $.each(vids,function(i,ea){
									div = $("<div/>").addClass("youtubeItem");
									$(div).click(
										function(){
											loadNewVideo(ea.videoId, 0);
										});
									var img = $('<img/>').attr({src: ea.thumbUrl});
									var cap = $('<p/>').text("\'" + ea.caption + "\'");
									var votes = $('<p/>').text("Votes: "+ (ea.votes || 0));
									var closeLink = $('<a/>').text("X").click(
										function(){
											self.model.deleteObject(ea);
										});
									var table = $('<table/>').addClass('playlistItemTable');
									var tr = $('<tr/>');
									var imgCell = $('<td/>').append(img).addClass('imgCell');
									var textCell = $('<td/>').append(cap).append(votes).addClass('textCell');
									var ctrlCell = $('<td/>').append(closeLink).addClass('ctrlCell');
									$(tr).append(imgCell);
									$(tr).append(textCell);
									$(tr).append(ctrlCell);
									$(table).append(tr);
									$(div).append(table);
									$("#playlist").append(div);
								});


						 // Update the party name

						 $('#mainTitle').text("In Party: " + self.model.getName());

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
				 var self = this;
				 this.partyProp = new PartyProp("party_prop");
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
						 board = new PartyWare.Party(self.partyProp);
					 },
					 initialExtras: [this.partyProp]
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
				 $("#qrCode").attr('src', "http://qrcode.kaywa.com/img.php?d=" +  jx.getInvitationURI());
			 };


		 })();
