package com.esc.test.apps.viewmodels;

import static com.esc.test.apps.other.MoveUtils.getCubeIds;
import static com.esc.test.apps.utils.TutAction.FLASH;
import static com.esc.test.apps.utils.TutAction.RESTART;

import android.app.Application;
import android.os.Handler;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.esc.test.apps.R;
import com.esc.test.apps.entities.PlayerInstruction;
import com.esc.test.apps.pojos.CubeID;
import com.esc.test.apps.utils.SingleLiveEvent;
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
    private final SingleLiveEvent<String> _pcMove = new SingleLiveEvent<>();
    public final SingleLiveEvent<String> pcMove = _pcMove;
    private final LiveData<String> lastMove = _lastMove;
    private int turnColor, notTurnColor;
    private int crossDrawable, circleDrawable, lastCross, lastCircle;
    public final int confirmColour;
    private final Random rand;
    private final Application app;
    public PlayerInstruction playerInstruction;

    private int userCount = 0, pcCount = 0;
    public String lastPos = "";

    @Inject
    public TutorialViewModel(Application app, Random rand) {
        super(app);
        populateGridLists();
        setDrawables();
        this.app = app;
        this.rand = rand;
        confirmColour = ContextCompat.getColor(app, R.color.colorTransBlue);
        new Handler().postDelayed(this::startPrompts, 100);
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
        circleDrawable = R.drawable.baseline_circle_24;
        crossDrawable = R.drawable.baseline_close_24;
        lastCircle = R.drawable.baseline_panorama_fish_eye_red;
        lastCross = R.drawable.baseline_close_red;
    }

    public void startPrompts() {
        playerInstruction = TutorialInstructions.user.get(0);
        nextPrompt();
    }

    public void nextInstruction(boolean isPc) {
        if (isPc && !checkNextInstruction().getAction().equals(RESTART)) {
            _pcMove.setValue(TutorialInstructions.pc.get(pcCount).getPos());
            pcCount ++;
        }
        userCount ++;
        playerInstruction = TutorialInstructions.user.get(userCount);
        nextPrompt();
    }

    private void nextPrompt() {
        if (playerInstruction.getAction() == FLASH) _flash.setValue(playerInstruction);
        _instructionText.setValue(playerInstruction.getPrompt());
    }

    public void wrongSquare() {
        _instructionText.setValue(app.getString(R.string.error_prompt));
        if (playerInstruction.getPos().equals(checkNextInstruction().getPos())) userCount ++;
    }

    private PlayerInstruction checkNextInstruction() {
       return TutorialInstructions.user.get(userCount + 1);
    }

    public ArrayList<CubeID[]> getLayerIDs() { return layerIDs; }

    public void setCubes(int z) { getCubeIds(layerIDs.get(z), z); }
}
