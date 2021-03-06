'use strict';

var RNGaplessSound = require('react-native').NativeModules.RNGaplessSound;
var IsAndroid = RNGaplessSound.IsAndroid;
var IsWindows = RNGaplessSound.IsWindows;
var resolveAssetSource = require("react-native/Libraries/Image/resolveAssetSource");
var nextKey = 0;

function isRelativePath(path) {
  return !/^(\/|http(s?))/.test(path);
}

function Sound(filename, basePath, onError, options) {
  var asset = resolveAssetSource(filename);
  if (asset) {
    this._filename = asset.uri;
    onError = basePath;
  } else {
    this._filename = basePath ? basePath + '/' + filename : filename;

    if (IsAndroid && !basePath && isRelativePath(filename)) {
      this._filename = filename.toLowerCase().replace(/\.[^.]+$/, '');
    }
  }

  this._loaded = false;
  this._key = nextKey++;
  this._duration = -1;
  this._numberOfChannels = -1;
  this._volume = 1;
  this._pan = 0;
  this._numberOfLoops = 0;
  this._speed = 1;

  RNGaplessSound.prepare(this._filename, this._key, options || {}, (error, props) => {
    if (props) {
      if (typeof props.duration === 'number') {
        this._duration = props.duration;
      }
      if (typeof props.numberOfChannels === 'number') {
        this._numberOfChannels = props.numberOfChannels;
      }
    }
    if (error === null) {
      this._loaded = true;
    }
    onError && onError(error);
  });
}

Sound.prototype.isLoaded = function() {
  return this._loaded;
};

Sound.prototype.play = function(onEnd) {
  if (this._loaded) {
    RNGaplessSound.play(this._key, (successfully) => onEnd && onEnd(successfully));
  } else {
    onEnd && onEnd(false);
  }
  return this;
};

Sound.prototype.pause = function(callback) {
  if (this._loaded) {
    RNGaplessSound.pause(this._key, () => { callback && callback() });
  }
  return this;
};

Sound.prototype.stop = function(callback) {
  if (this._loaded) {
    RNGaplessSound.stop(this._key, () => { callback && callback() });
  }
  return this;
};

Sound.prototype.release = function() {
  if (this._loaded) {
    RNGaplessSound.release(this._key);
  }
  return this;
};

Sound.prototype.getDuration = function() {
  return this._duration;
};

Sound.prototype.getNumberOfChannels = function() {
  return this._numberOfChannels;
};

Sound.prototype.getVolume = function() {
  return this._volume;
};

Sound.prototype.setVolume = function(value) {
  this._volume = value;
  if (this._loaded) {
    if (IsAndroid || IsWindows) {
      RNGaplessSound.setVolume(this._key, value, value);
    } else {
      RNGaplessSound.setVolume(this._key, value);
    }
  }
  return this;
};

Sound.prototype.getPan = function() {
  return this._pan;
};

Sound.prototype.setPan = function(value) {
  if (this._loaded) {
    RNGaplessSound.setPan(this._key, this._pan = value);
  }
  return this;
};

Sound.prototype.getNumberOfLoops = function() {
  return this._numberOfLoops;
};

Sound.prototype.setNumberOfLoops = function(value) {
  this._numberOfLoops = value;
  if (this._loaded) {
    if (IsAndroid || IsWindows) {
      RNGaplessSound.setLooping(this._key, !!value);
    } else {
      RNGaplessSound.setNumberOfLoops(this._key, value);
    }
  }
  return this;
};

Sound.prototype.setSpeed = function(value) {
  this._setSpeed = value;
  if (this._loaded) {
    if (!IsWindows) {
      RNGaplessSound.setSpeed(this._key, value);
    }
  }
  return this;
};

Sound.prototype.getCurrentTime = function(callback) {
  if (this._loaded) {
    RNGaplessSound.getCurrentTime(this._key, callback);
  }
};

Sound.prototype.setCurrentTime = function(value) {
  if (this._loaded) {
    RNGaplessSound.setCurrentTime(this._key, value);
  }
  return this;
};

// android only
Sound.prototype.setSpeakerphoneOn = function(value) {
  if (IsAndroid) {
    RNGaplessSound.setSpeakerphoneOn(this._key, value);
  }
};

// ios only

// This is deprecated.  Call the static one instead.

Sound.prototype.setCategory = function(value) {
  Sound.setCategory(value, false);
}

Sound.enable = function(enabled) {
  RNGaplessSound.enable(enabled);
};

Sound.enableInSilenceMode = function(enabled) {
  if (!IsAndroid && !IsWindows) {
    RNGaplessSound.enableInSilenceMode(enabled);
  }
};

Sound.setCategory = function(value, mixWithOthers = false) {
  if (!IsAndroid && !IsWindows) {
    RNGaplessSound.setCategory(value, mixWithOthers);
  }
};

Sound.MAIN_BUNDLE = RNGaplessSound.MainBundlePath;
Sound.DOCUMENT = RNGaplessSound.NSDocumentDirectory;
Sound.LIBRARY = RNGaplessSound.NSLibraryDirectory;
Sound.CACHES = RNGaplessSound.NSCachesDirectory;

module.exports = Sound;
