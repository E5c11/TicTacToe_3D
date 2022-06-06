package com.esc.test.apps.viewmodels;

import static com.esc.test.apps.other.MoveUtils.getCubeIds;
import static com.esc.test.apps.utils.TutAction.FLASH;

import android.app.Application;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.esc.test.apps.R;
import com.esc.test.apps.entities.PlayerInstruction;
import com.esc.test.apps.pojos.CubeID;
import com.esc.test.apps.utils.SingleLiveEvent;
import com.esc.test.apps.utils.TutAction;
import com.esc.test.apps.utils.TutorialInstructions;

import java.util.ArrayList;
import java.util.Random;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TutorialViewModel extends AndroidViewModel {

    private final ArrayList<CubeID[]> layerIDs = new ArrayList<>();
    private final MutableLiveData<String> _instructionText = new MutableLiveData<>();
    public final LiveData<String> instructionText = _instructionText;
    private final MutableLiveData<String> _lastMove = new MutableLiveData<>();
    private final SingleLiveEvent<PlayerInstruction> _flash = new SingleLiveEvent<>();
    public final SingleLiveEvent<PlayerInstruction> flash = _flash;
    private final LiveData<String> lastMove = _lastMove;
    private final MutableLiveData<String> _pcMove = new MutableLiveData<>();
    private final LiveData<String> pcMove = _pcMove;
    private int turnColor, notTurnColor;
    private int crossDrawable, circleDrawable, lastCross, lastCircle;
    public final int confirmColor;
    private final Random rand;
    public PlayerInstruction playerInstruction;

    private int userCount = 0;
    public String lastPos = "";

    @Inject
    public TutorialViewModel(Application app, Random rand) {
        super(app);
        populateGridLists();
        setDrawables();
        this.rand = rand;
        confirmColor = ContextCompat.getColor(app, R.color.colorTransBlue);
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
        userCount ++;
        playerInstruction = TutorialInstructions.user.get(userCount);
        nextPrompt();
    }

    public void skipInstruction() {
        userCount += 2;
        playerInstruction = TutorialInstructions.user.get(userCount);
        nextPrompt();
    }

    private void nextPrompt() {
        if (playerInstruction.getAction() == FLASH) _flash.setValue(playerInstruction);
        _instructionText.setValue(playerInstruction.getPrompt());
    }

    public ArrayList<CubeID[]> getLayerIDs() { return layerIDs; }

    public void setCubes(int z) { getCubeIds(layerIDs.get(z), z); }
}
