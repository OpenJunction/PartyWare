var ytplayer = null;

function onYouTubePlayerReady(playerId) {
    ytplayer = document.getElementById("myytplayer");
    ytplayer.addEventListener("onStateChange", "onytplayerStateChange");
    ytplayer.addEventListener("onError", "onPlayerError");
}

function onPlayerError(errorCode) {
    alert("An error occured: " + errorCode);
}

function onytplayerStateChange(newState) {}

// functions for the api calls

function loadNewVideo(id, startSeconds) {
    if (ytplayer) {
        ytplayer.loadVideoById(id, parseInt(startSeconds));
    }
}

function cueNewVideo(id, startSeconds) {
    if (ytplayer) {
        ytplayer.cueVideoById(id, startSeconds);
    }
}

function play() {
    if (ytplayer) {
        ytplayer.playVideo();
    }
}

function pause() {
    if (ytplayer) {
        ytplayer.pauseVideo();
    }
}

function stop() {
    if (ytplayer) {
        ytplayer.stopVideo();
    }
}

function getPlayerState() {
    if (ytplayer) {
        return ytplayer.getPlayerState();
    }
}

function seekTo(seconds) {
    if (ytplayer) {
        ytplayer.seekTo(seconds, true);
    }
}

function getBytesLoaded() {
    if (ytplayer) {
        return ytplayer.getVideoBytesLoaded();
    }
}

function getBytesTotal() {
    if (ytplayer) {
        return ytplayer.getVideoBytesTotal();
    }
}

function getCurrentTime() {
    if (ytplayer) {
        return ytplayer.getCurrentTime();
    }
}

function getDuration() {
    if (ytplayer) {
        return ytplayer.getDuration();
    }
}

function getStartBytes() {
    if (ytplayer) {
        return ytplayer.getVideoStartBytes();
    }
}

function mute() {
    if (ytplayer) {
        ytplayer.mute();
    }
}

function unMute() {
    if (ytplayer) {
        ytplayer.unMute();
    }
}

function getEmbedCode() {
    alert(ytplayer.getVideoEmbedCode());
}

function getVideoUrl() {
    alert(ytplayer.getVideoUrl());
}

function setVolume(newVolume) {
    if (ytplayer) {
        ytplayer.setVolume(newVolume);
    }
}

function getVolume() {
    if (ytplayer) {
        return ytplayer.getVolume();
    }
}

function clearVideo() {
    if (ytplayer) {
        ytplayer.clearVideo();
    }
}



