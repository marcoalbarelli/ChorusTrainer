package eu.marcoalbarelli.android.chorustrainer.midi;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.meta.Tempo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import eu.marcoalbarelli.android.chorustrainer.Constants;

/**
 * Created by marco on 04/07/13.
 */
public class MidiManager {

    private final static String TAG = MidiManager.class.getSimpleName();

    private MidiFile    source;
    private MidiFile    modifiedSource;
    private Context     context;
    private File        modifiedFile;

    public MidiManager(Context context, MidiFile source){
        this.context = context;
        this.source = source;
        this.modifiedSource = new MidiFile();
        this.modifiedSource.setResolution(source.getResolution());
        this.modifiedSource.setType(source.getType());
    }

    private File getTempFile(Context context, String url) {
        File file = null;
        try {
            String fileName = Uri.parse(url).getLastPathSegment();
            file = File.createTempFile(fileName, null, context.getCacheDir());
        }catch (IOException e) {
                // Error while creating file
        }
        return file;
    }

    public File getModifiedFile(){
        return modifiedFile;
    }

    private File saveModifiedFile(){
        File tmp = getTempFile(context,Constants.OUTPUT_TMP_FILE_NAME);
        try{
            modifiedSource.writeToFile(tmp);
        } catch(FileNotFoundException e){
            Log.e(TAG, e.getMessage());
        } catch(IOException e){
            Log.e(TAG,e.getMessage());
        }
        modifiedFile = tmp;
        return tmp;
    }

    public void removeTrackFromFile(int trackId){

        return;
    }
    public void emphasizeTrackInFile(int trackId){

        /**
         * The name of the method should be dampenOtherTracks, since it doesn't actually emphasize the chosen track, but I think that was an horrible name
         */
        return;
    }
    public void reduceTempoInFileByFactor(float newTempoFactor){
        if(modifiedSource.getTracks().size()<1){
            return;
        }
        ArrayList<MidiTrack> tracks=source.getTracks();

        for(MidiTrack T:tracks){
            Iterator<MidiEvent> it = T.getEvents().iterator();
            while(it.hasNext()) {
                MidiEvent E = it.next();

                if(E.getClass().equals(Tempo.class)) {

                    Tempo tempo = (Tempo)E;
                    tempo.setBpm(tempo.getBpm() / newTempoFactor);
                }
            }
        }
        saveModifiedFile();
        return;
    }


    public void isolateTrack(int trackNumber){

        modifiedSource.addTrack(source.getTracks().get(0));
        for (int i = 0; i < source.getTracks().size(); i++){
            if(i==trackNumber){
                modifiedSource.addTrack(source.getTracks().get(i));
            }
        }
        saveModifiedFile();
        return ;

        //TODO: Split this code into appropriate functions

        // 2. Do some editing to the file
        // 2a. Strip out anything but notes from track 1
        //MidiTrack T = mf.getTracks().get(trackNumber);

        /*
        // It's a bad idea to modify a set while iterating, so we'll collect
        // the events first, then remove them afterwards
        Iterator<MidiEvent> it = T.getEvents().iterator();
        ArrayList<MidiEvent> eventsToRemove = new ArrayList<MidiEvent>();

        while(it.hasNext()) {
            MidiEvent E = it.next();

            if(!E.getClass().equals(NoteOn.class) && !E.getClass().equals(NoteOff.class)) {
                eventsToRemove.add(E);
            }
        }

        for(MidiEvent E : eventsToRemove) {
            T.removeEvent(E);
        }

        // 2b. Completely remove track 2
        mf.removeTrack(2);

        // 2c. Reduce the tempo by half

        */

        // 3. Save the file back to disk

    }


}
