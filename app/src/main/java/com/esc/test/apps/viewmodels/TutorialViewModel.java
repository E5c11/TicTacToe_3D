package com.esc.test.apps.viewmodels;

import static com.esc.test.apps.other.MoveUtils.getCubeIds;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.esc.test.apps.R;
import com.esc.test.apps.pojos.CubeID;
import com.esc.test.apps.pojos.MoveUpdate;
import com.esc.test.apps.utils.SingleLiveEvent;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TutorialViewModel extends AndroidViewModel {

    private final ArrayList<CubeID[]> layerIDs = new ArrayList<>();
    private final MutableLiveData<String> _instruction = new MutableLiveData<>();
    public final LiveData<String> instruction = _instruction;
    private final MutableLiveData<String> _lastMove = new MutableLiveData<>();
    private final LiveData<String> lastMove = _lastMove;
    private final MutableLiveData<String> _pcMove = new MutableLiveData<>();
    private final LiveData<String> pcMove = _pcMove;
    private int turnColor, notTurnColor;
    private int crossDrawable, circleDrawable, lastCross, lastCircle;
    private int count;
    public String lastPos = "";

    @Inject
    public TutorialViewModel(Application app) {
        super(app);
        populateGridLists();
        setDrawables();
    }

    private void populateGridLists() {
        layerIDs.add(new CubeID[16]);
        layerIDs.add(new CubeID[16]);
        layerIDs.add(new CubeID[16]);
        layerIDs.add(new CubeID[16]);
    }

    private void setDrawables() {
        turnColor = R.color.colorAccent;
        notTurnColor = R.color.colorPrimary;
        circleDrawable = R.drawable.baseline_panorama_fish_eye_24;
        crossDrawable = R.drawable.baseline_close_24;
        lastCircle = R.drawable.baseline_panorama_fish_eye_red;
        lastCross = R.drawable.baseline_close_red;
    }

    public void nextInstruction() {
        count ++;
        nextPrompt();
    }

    private void nextPrompt() {
        _instruction.setValue("Click the square again to confirm your move");
    }

    public ArrayList<CubeID[]> getLayerIDs() { return layerIDs; }

    public void setCubes(int z) { getCubeIds(layerIDs.get(z), z); }
}
