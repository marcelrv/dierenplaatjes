/*
 * Copyright (c) 2024.  Marcel Verpaalen
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 */
package mv.dierenplaatjes;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DierenPlayer {

    public static final String AUDIO_FILE_EXTENSION = ".wav";
    public static final String SOUND_PREFIX = "s";
    private static final String DEFAULT_ERROR_FILENAME = "ohoh.mp3";


    private final ConcurrentLinkedQueue<File>
            playlist = new ConcurrentLinkedQueue<>();

    private final Map<Integer, Integer> questionMap = new HashMap<>();
    private boolean isPlaying = false;
    private static final String TAG = "DierenPlayer";
    Context context;

    DierenPlayer(Context context) {
        this.context = context;
    }

    private MediaPlayer mediaPlayer;


    private void addToList(@Nullable File file) {
        if (file != null) {
            playlist.add(file);
        } else {
            Log.e(TAG, "File is null. Skipped from playlist.");

        }
    }

    public void play(int cardId, PlayType type) {
        isPlaying = true;
        Random random = new Random();

        Integer sequenceNumber = questionMap.getOrDefault(cardId, random.nextInt(4) );
        sequenceNumber += 1;
        if (sequenceNumber > 4 || sequenceNumber < 1) {
            sequenceNumber = 1;
        }
        questionMap.put(cardId, sequenceNumber);

        if (type != PlayType.QUESTION_ONLY) {
            File animal = getFile(cardId, SoundType.ANIMAL);
            if (animal != null) addToList(animal);
            else addToList(getFileFromAppSpecificStorage(DEFAULT_ERROR_FILENAME));
        }
        if (type == PlayType.ALL) {
            File question = getFile(cardId, SoundType.QUESTION, sequenceNumber);
            if (question != null) {
                addToList(question);
                addToList(getFileFromAppSpecificStorage("Z_B.wav"));
                addToList((getFile(cardId, SoundType.ANSWER, sequenceNumber)));
            } else {
                addToList((getFile(cardId, SoundType.COMBINED, sequenceNumber)));
            }
        }
        Log.d(TAG, String.format("Starting playlist Dier %02d  vraag %01d  %s", cardId, sequenceNumber , playlist.toString()));
        startPlaylistNext();
    }

    private void startPlaylistNext() {
        if (playlist.isEmpty()) {
            stopPlaying();
        } else {
            final File file = playlist.poll();
            if (file != null) {
                Log.d(TAG, "Starting next file in playlist.");
                playAudioFile(file);
            } else {
                Log.d(TAG, "File is null.");
            }
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void stopPlaying() {
        playlist.clear();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
        }
    }

    private @Nullable File getFileFromAppSpecificStorage(String filename) {
        File appSpecificDir = context.getExternalFilesDir(null);  // External app-specific storage
        if (appSpecificDir != null) {
            File file = new File(appSpecificDir, filename);
            if (file.exists()) {
                return file;
            }
            Log.e(TAG, "File not found: " + file.getAbsolutePath());
        } else {
            Log.e(TAG, "App-specific storage directory not found.");
        }
        return null;
    }

    public @Nullable File getFile(int cardId, SoundType type) {
        return getFile(cardId, type, 0);
    }

    /**
     * Retrieves the sound file for the given card ID, sound type, and sequence number.
     *
     * @param cardId         The ID of the card.
     * @param type           The type of sound.
     * @param sequenceNumber The sequence number of the sound.
     * @return The sound file, or null if it does not exist.
     */
    public @Nullable File getFile(int cardId, SoundType type, int sequenceNumber) {
        String fileName = String.format(Locale.GERMAN, "%s%02d%s%01d%s%s",
                SOUND_PREFIX, cardId, type.getPrefix(), sequenceNumber, type.getSuffix(), AUDIO_FILE_EXTENSION);
        return getFileFromAppSpecificStorage(fileName);
    }

    public void playAudioFile(File file) {
        Log.d(TAG, "Playing audio file: " + file.getAbsolutePath());
        if (mediaPlayer != null) {
            mediaPlayer.release();  // Release the previous instance if it exists
        }
        isPlaying = true;
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(file.getAbsolutePath());  // Set the file path
            mediaPlayer.prepare();  // Prepare the player
            mediaPlayer.start();    // Start playback
            mediaPlayer.setOnCompletionListener(mp -> startPlaylistNext());
            Log.d(TAG, "Playing file: " + file.getAbsolutePath());

        } catch (IOException e) {
            Log.e(TAG, "Error playing file: " + e.getMessage());
        }
    }

}
