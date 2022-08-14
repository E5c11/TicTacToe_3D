package com.esc.test.apps.viewmodels;

import static com.esc.test.apps.adapters.move.MoveUtils.getCubeIds;
import static com.esc.test.apps.utils.TutAction.END;
import static com.esc.test.apps.utils.TutAction.FLASH;
import static com.esc.test.apps.utils.TutAction.RESTART;

import android.app.Application;
import android.os.Handler;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.esc.test.apps.R;
import com.esc.test.apps.data.datastore.UserPreferences;
import com.esc.test.apps.data.entities.PlayerInstruction;
import com.esc.test.apps.data.pojos.CubeID;
import com.esc.test.apps.utils.SingleLiveEvent;
import com.esc.test.apps.utils.TutorialInstructions;

import java.util.ArrayList;
import java.util.List;
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
    private final SingleLiveEvent<String> _restart = new SingleLiveEvent<>();
    public final SingleLiveEvent<String> restart = _restart;
    private final SingleLiveEvent<List<String>> _winner = new SingleLiveEvent<>();
    public final SingleLiveEvent<List<String>> winner = _winner;
    private final SingleLiveEvent<Boolean> _end = new SingleLiveEvent<>();
    public final SingleLiveEvent<Boolean> end = _end;
    private final UserPreferences userPref;
    private int turnColor, notTurnColor;
    private int crossDrawable, circleDrawable, lastCross, lastCircle;
    public final int confirmColour;
    private final Random rand;
    private final Application app;
    public PlayerInstruction playerInstruction;

    private int userCount = 0, pcCount = 0, gameCount = 0;
    public String lastPos = "";
    public String lastAltPos = "";
    public String line = null;

    @Inject
    public TutorialViewModel(Application app, Random rand, UserPreferences userPref) {
        super(app);
        this.userPref = userPref;
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
        if (isPc && !checkNextInstruction().getAction().equals(RESTART) && !checkNextInstruction().getAction().equals(END)) {
            _pcMove.setValue(TutorialInstructions.pc.get(pcCount).getPos());
            pcCount ++;
        }
        userCount ++;
        playerInstruction = TutorialInstructions.user.get(userCount);
        nextPrompt();
    }

    private void nextPrompt() {
        if (line == null && playerInstruction.getAltPos() != null) line = "";
        if (playerInstruction.getAction() == FLASH) _flash.setValue(playerInstruction);
        else if (playerInstruction.getAction() == RESTART) {
            checkLine();
            lastAltPos = ""; lastPos = ""; gameCount ++; line = null;
            new Handler().postDelayed(() -> _restart.setValue("restart"), 3000);
            new Handler().postDelayed(() -> nextInstruction(false), 3100);
        } else if (playerInstruction.getAction() ==  END) {
            checkLine();
            userPref.updateTutorialJava(true);
//            userDetails.setTutorial(true);
            new Handler().postDelayed(() ->
                    _instructionText.setValue(app.getString(R.string.nineteenth_instruction)), 2000);
            new Handler().postDelayed(() -> _end.postValue(true), 4000);
        }
        _instructionText.setValue(playerInstruction.getPrompt());

    }

    public void wrongSquare() {
        _instructionText.setValue(app.getString(R.string.error_prompt));
        if (playerInstruction.getPos().equals(checkNextInstruction().getPos())) userCount ++;
    }

    private void checkLine() {
        if (line == null || !line.equals("second")) line = "first";
        _winner.setValue(TutorialInstructions.winningRow.get(line.equals("first") ? gameCount : ++gameCount));
    }

    private PlayerInstruction checkNextInstruction() {
       return TutorialInstructions.user.get(userCount + 1);
    }

    public ArrayList<CubeID[]> getLayerIDs() { return layerIDs; }

    public void setCubes(int z) { getCubeIds(layerIDs.get(z), z); }
}
