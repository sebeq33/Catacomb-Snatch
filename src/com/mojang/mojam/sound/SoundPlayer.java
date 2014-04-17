package com.mojang.mojam.sound;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

import paulscode.sound.Library;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.libraries.LibraryJavaSound;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

import com.mojang.mojam.MojamComponent;
import com.mojang.mojam.Options;

public class SoundPlayer implements ISoundPlayer {

	private final Class<? extends Library> libraryType;
	private Set<String> loaded = new TreeSet<String>();
	
	private SoundSystem soundSystem;
	private boolean oggPlaybackSupport = true;
	private boolean wavPlaybackSupport = true;
	private boolean muted = false;

    private float volume = Options.getAsFloat(Options.VOLUME, "1.0f");
	private float musicVolume = Options.getAsFloat(Options.MUSIC, "1.0f");
	private float soundVolume = Options.getAsFloat(Options.SOUND, "1.0f");

	private static final int MAX_SOURCES_PER_SOUND = 5;
	private int nextSong = 0;

	public SoundPlayer() {
		
		try {
			SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
		} catch (SoundSystemException ex) {
			oggPlaybackSupport = false;
		}

		try {
			SoundSystemConfig.setCodec("wav", CodecWav.class);
		} catch (SoundSystemException ex) {
			wavPlaybackSupport = false;
		}

		boolean aLCompatible = SoundSystem.libraryCompatible(LibraryLWJGLOpenAL.class);
		if (aLCompatible) {
			libraryType = LibraryLWJGLOpenAL.class; // OpenAL
		} else {
			libraryType = LibraryJavaSound.class; // Java Sound
	    } 

		try {
			setSoundSystem(new SoundSystem(libraryType));
		} catch (SoundSystemException ex) {
			setSoundSystem(null);
		}
		
		if (getSoundSystem() != null) {
			getSoundSystem().setMasterVolume(volume);
			getSoundSystem().setVolume(BACKGROUND_TRACK, musicVolume);
		}
	}

	private boolean hasOggPlaybackSupport() {
		return oggPlaybackSupport && getSoundSystem() != null;
	}

	private boolean hasWavPlaybackSupport() {
		return wavPlaybackSupport && getSoundSystem() != null;
	}

	private boolean isPlaying(String sourceName) {
		if (hasOggPlaybackSupport()) {
			return getSoundSystem().playing(sourceName);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.mojang.mojam.sound.ISoundPlayer#startTitleMusic()
	 */
	@Override
	public void startTitleMusic() {
	    musicVolume = muted ? 0 : Options.getAsFloat(Options.MUSIC, "1.0f");
	    
	    if (hasOggPlaybackSupport() && !muted) {
	    	if (isPlaying(BACKGROUND_TRACK)) stopBackgroundMusic();

			URL backgroundTrack = fileExist("ThemeTitle.ogg");
			if (backgroundTrack != null){
				    getSoundSystem().backgroundMusic(BACKGROUND_TRACK, backgroundTrack, backgroundTrack.getPath(), true);
			}
	    }

	    getSoundSystem().setVolume(BACKGROUND_TRACK, musicVolume);
	}

	/* (non-Javadoc)
	 * @see com.mojang.mojam.sound.ISoundPlayer#startEndMusic()
	 */
	@Override
	public void startEndMusic() {
		 musicVolume = muted ? 0 : Options.getAsFloat(Options.MUSIC, "1.0f");
	    
	    if (hasOggPlaybackSupport() && !muted) {
			if (isPlaying(BACKGROUND_TRACK)) stopBackgroundMusic();

			URL backgroundTrack = fileExist("ThemeEnd.ogg");
			if (backgroundTrack != null){
				    getSoundSystem().backgroundMusic(BACKGROUND_TRACK, backgroundTrack, backgroundTrack.getPath(), true);
			}
	    }

	    getSoundSystem().setVolume(BACKGROUND_TRACK, musicVolume);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mojang.mojam.sound.ISoundPlayer#startBackgroundMusic()
	 */
	@Override
	public void startBackgroundMusic() {
		 musicVolume = muted ? 0 : Options.getAsFloat(Options.MUSIC, "1.0f");
	    
	    if (hasOggPlaybackSupport() && !muted) {
	    	System.out.println("*** startBackgroundMusic ***");
	    	
			if (isPlaying(BACKGROUND_TRACK))
			    stopBackgroundMusic();
	
			nextSong++;
			if (nextSong>4) nextSong = 1;
			//nextSong = TurnSynchronizer.synchedRandom.nextInt(4)+1;
			
				URL backgroundTrack = fileExist("Background " + nextSong + ".ogg");
				if (backgroundTrack != null){
					System.out.println("next song: " + "Background " + nextSong + ".ogg");
					getSoundSystem().backgroundMusic(BACKGROUND_TRACK, backgroundTrack, backgroundTrack.getPath(), true);
				}
	    }

	    getSoundSystem().setVolume(BACKGROUND_TRACK, musicVolume);
	}

	/* (non-Javadoc)
	 * @see com.mojang.mojam.sound.ISoundPlayer#stopBackgroundMusic()
	 */
	@Override
	public void stopBackgroundMusic() {
		if (hasOggPlaybackSupport()) {
			getSoundSystem().stop(BACKGROUND_TRACK);
		}
	}

	/* (non-Javadoc)
	 * @see com.mojang.mojam.sound.ISoundPlayer#setListenerPosition(float, float)
	 */
	@Override
	public void setListenerPosition(float x, float y) {
		getSoundSystem().setListenerPosition(x, y, 50);
	}

	/* (non-Javadoc)
	 * @see com.mojang.mojam.sound.ISoundPlayer#playSound(java.lang.String, float, float)
	 */
	@Override
	public boolean playSound(String sourceName, float x, float y) {
		return playSound(sourceName, x, y, false);
	}

	/* (non-Javadoc)
	 * @see com.mojang.mojam.sound.ISoundPlayer#playSound(java.lang.String, float, float, boolean)
	 */
	@Override
	public boolean playSound(String sourceName, float x, float y, boolean blocking) {
		return playSound(sourceName, x, y, blocking, 0);
	}

	private boolean playSound(String sourceName, float x, float y, boolean blocking, int index) {
		updateMute();
		
		if (index < MAX_SOURCES_PER_SOUND && !muted && soundVolume >= 0.01 && hasWavPlaybackSupport()) {
			String indexedSourceName = sourceName + index;
			if (!loaded.contains(indexedSourceName)) {
				getSoundSystem().newSource(false, indexedSourceName, SoundPlayer.class.getResource(sourceName), sourceName, false, x, y, 0, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
				getSoundSystem().loadSound(sourceName);
				loaded.add(indexedSourceName);
			} else if (isPlaying(indexedSourceName)) {
				if (blocking) {
					return false;
				}

				// Source already playing, create new source for same sound effect.
				return playSound(sourceName, x, y, false, index + 1);
			}
			
			if (getSoundSystem().playing(indexedSourceName) /*|| libraryType.equals(LibraryJavaSound.class)*/) {
				//System.out.println(getSoundSystem().playing(indexedSourceName) + " ///// " + libraryType.equals(LibraryJavaSound.class));
				getSoundSystem().stop(indexedSourceName);
			}
			
			getSoundSystem().setPriority(indexedSourceName, false);
			getSoundSystem().setPosition(indexedSourceName, x, y, 0);
			getSoundSystem().setAttenuation(indexedSourceName, SoundSystemConfig.ATTENUATION_ROLLOFF);
			getSoundSystem().setDistOrRoll(indexedSourceName, SoundSystemConfig.getDefaultRolloff());
			getSoundSystem().setPitch(indexedSourceName, 1.0f);
			getSoundSystem().setVolume(indexedSourceName, soundVolume);
			getSoundSystem().play(indexedSourceName);
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.mojang.mojam.sound.ISoundPlayer#shutdown()
	 */
	@Override
	public void shutdown() {
		if (getSoundSystem() != null) {
			getSoundSystem().cleanup();
		}
	}

	/* (non-Javadoc)
	 * @see com.mojang.mojam.sound.ISoundPlayer#isMuted()
	 */
	@Override
	public boolean isMuted() {
		return muted;
	}

	/* (non-Javadoc)
	 * @see com.mojang.mojam.sound.ISoundPlayer#setMuted(boolean)
	 */
	@Override
	public void setMuted(boolean muted) {
		this.muted = muted;
	}

	public void setSoundSystem(SoundSystem soundSystem) {
		this.soundSystem = soundSystem;
	}

	public SoundSystem getSoundSystem() {
		return soundSystem;
	}
	
	/** return null if file not found else send back the completed URL**/
	public URL fileExist(String s){
		URL url = null;
		if ((new File(MojamComponent.getMojamDir(), "/resources/sound/" + s)).exists()) 
		{
		    try {
		    	url = new File(MojamComponent.getMojamDir(), "/resources/sound/" + s).toURI().toURL();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		else{
			url = SoundPlayer.class.getResource("/sound/" + s);
		}
		
		if (url == null) System.out.println("Ressource file not found : " + s);
		
		return url;
	}
	
	public void updateMute() {
		muted = getSoundSystem().getMasterVolume() < 0.01 || (Options.getAsFloat(Options.MUSIC, "1.0f") < 0.01 && Options.getAsFloat(Options.SOUND, "1.0f") < 0.01);
		volume = muted ? 0 : Options.getAsFloat(Options.VOLUME, "1.0f");
		musicVolume = muted ? 0 : Options.getAsFloat(Options.MUSIC, "1.0f");
		soundVolume = muted ? 0 : Options.getAsFloat(Options.SOUND, "1.0f");
		
		/*System.out.println("\nmasterVolume : " +  getSoundSystem().getMasterVolume());
		System.out.println("volume : " + volume);
		System.out.println("musicVolume : " + musicVolume);
		System.out.println("soundVolume : " + soundVolume);
		System.out.println("muted : " + muted);*/
	}
}
