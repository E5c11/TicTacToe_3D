package com.esc.test.apps.viewmodels.board;

import static com.esc.test.apps.adapters.move.MoveUtils.getCubeIds;
import static com.esc.test.apps.utils.Utils.dispose;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.esc.test.apps.R;
import com.esc.test.apps.adapters.CubeAdapter;
import com.esc.test.apps.adapters.move.MovesFactory;
import com.esc.test.apps.data.datastore.GamePreferences;
import com.esc.test.apps.data.entities.Game;
import com.esc.test.apps.data.pojos.CubeID;
import com.esc.test.apps.data.pojos.MoveInfo;
import com.esc.test.apps.data.pojos.MoveUpdate;
import com.esc.test.apps.repositories.GameRepository;
import com.esc.test.apps.repositories.MoveRepository;
import com.esc.test.apps.utils.SingleLiveEvent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class PassPlayBoardViewModel extends ViewModel {

    private final ArrayList<CubeID[]> layerIDs = new ArrayList<>();
    private String lastPos;
    private final MutableLiveData<String> circleScore = new MutableLiveData<>();
    private final MutableLiveData<String> crossScore = new MutableLiveData<>();
    private final MutableLiveData<Integer> xTurn = new MutableLiveData<>();
    private final MutableLiveData<Integer> oTurn = new MutableLiveData<>();
    private final MutableLiveData<List<int[]>> winnerLine = new MutableLiveData<>();
    public final SingleLiveEvent<MoveUpdate> lastMove = new SingleLiveEvent<>();
    private final Flowable<String> turn;
    private final GamePreferences gamePref;
    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;
    private final MovesFactory moves;
    private static final String TAG = "myT";
    private int turnColor, notTurnColor;
    private int crossDrawable, circleDrawable, lastCross, lastCircle;
    private int[] lastPosition;
    private int lastPiecePlayed;
    private final Application app;
    private Disposable d, f, t;

    @Inject
    public PassPlayBoardViewModel(MovesFactory moves, Application app, GamePreferences gamePref,
                                  MoveRepository moveRepository, GameRepository gameRepository
    ) {
        populateGridLists();
        setDrawables();
        this.moves = moves;
        this.gameRepository = gameRepository;
        this.moveRepository = moveRepository;
        this.gamePref = gamePref;
        turn = gameRepository.getTurn();
        this.app = app;
        setBeforeGame();
        Log.d(TAG, "PassPlayBoardViewModel: ");
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

    public ArrayList<CubeID[]> getLayerIDs() { return layerIDs; }

    public void setCubes(int z) { getCubeIds(layerIDs.get(z), z); }

    public void setBeforeGame() {
        clearOnlineGame();
        d = gamePref.getGamePreference().subscribeOn(Schedulers.io()).doOnNext( pref -> {
            circleScore.postValue(pref.getCircleScore());
            crossScore.postValue(pref.getCrossScore());
            dispose(d);
        }).subscribe();
    }

    public void clearSet() {
        gamePref.newSetJava();
        clearLocalGame();
        circleScore.setValue("0"); crossScore.setValue("0");
    }

    public void clearOnlineGame() {
        gamePref.newOnlineGameJava();
        insertNewGame();
        clearMoves();
        if (gameRepository.getPreviousStarter().toString().equals(app.getString(R.string.circle))) circleTurn();
        else crossTurn();
    }

    public void clearLocalGame() {
        lastPosition = null;
        lastPiecePlayed = 0;
        gamePref.newLocalGameJava();
        insertNewGame();
    }

    private void insertNewGame() {
        gameRepository.insertGame(new Game(
                "in progress", "cross", "", "opponent", "not started"));
    }

    public void addCircleScore() {
        if (circleScore.getValue() == null) circleScore.setValue("0");
        else circleScore.setValue(String.valueOf(Integer.parseInt(circleScore.getValue()) + 1));
        gamePref.updateCircleScoreJava(circleScore.getValue());
    }
    public MutableLiveData<String> getCircleScore() {return circleScore;}

    public void addCrossScore() {
        if (crossScore.getValue() == null) crossScore.setValue("0");
        else crossScore.setValue(String.valueOf(Integer.parseInt(crossScore.getValue()) + 1));
        gamePref.updateCrossScoreJava(crossScore.getValue());
    }
    public MutableLiveData<String> getCrossScore() {return crossScore;}

    public void setLastPos(String tag) { lastPos = tag; }
    public String getLastPos() { return lastPos; }

    public void newMove(CubeID cubeID) {
        d = turn.subscribeOn(Schedulers.io()).doOnNext(turn -> {
            t = gamePref.getGamePreference().subscribeOn(Schedulers.io()).doOnNext( pref -> {
                if (pref.getStarter().isEmpty()) {
                    gamePref.updateStarterJava(turn);
                    gameRepository.setStarter(turn);
                }
                moves.createMoves(cubeID.getCoordinates(), turn, null, false);
                dispose(t);
            }).subscribe();
            dispose(d);
        }).subscribe();
    }

    public void updateView(CubeID cubeID) {
        f = turn.subscribeOn(Schedulers.io()).doOnNext(turn -> {
            lastMove.postValue(new MoveUpdate(cubeID.getArrayPos(), turn));
            dispose(f);
        }).subscribe();
    }

    public void downloadedMove(MoveInfo move) {
        moves.createMoves(String.valueOf(move.getCoordinates()),
                String.valueOf(move.getPiece_played()), move.getMoveID(), false);
        if (String.valueOf(move.getPiece_played()).equals(app.getString(R.string.cross)))
            circleTurn();
        else crossTurn();
    }

    public void updateScore(String winner) {
        if (winner.equals(app.getString(R.string.cross))) addCrossScore();
        else addCircleScore();
        updateWinners();
    }

    public void crossTurn() {
        xTurn.setValue(turnColor);
        oTurn.setValue(notTurnColor);
        updateTurn(app.getString(R.string.cross));
    }

    public void circleTurn() {
        xTurn.setValue(notTurnColor);
        oTurn.setValue(turnColor);
        updateTurn(app.getString(R.string.circle));
    }

    public LiveData<Integer> getxTurn() { return xTurn; }

    public LiveData<Integer> getoTurn() { return oTurn; }

    public LiveData<String> getTurn() {
        return LiveDataReactiveStreams
            .fromPublisher(turn.subscribeOn(Schedulers.io())
                .map( result -> {
                    String winner = gamePref.getGamePreference().blockingSingle().getWinner();
                    if (winner.isEmpty()) return result;
                    else return null;
                }));
    }

    public LiveData<String> getWinner() {
        return LiveDataReactiveStreams.fromPublisher(
            gameRepository.getWinner().subscribeOn(Schedulers.io())
                .map( result -> {
                    String winner = gamePref.getGamePreference().blockingSingle().getWinner();
                    if (result != null && !result.equals("in progress") && !winner.isEmpty())
                        return result;
                    else return null;
                })
        );
    }

    public LiveData<String> getStarter() {
        return Transformations.map(moveRepository.getFirstMove(), starterResult -> {
            if (starterResult != null) {
                gamePref.updateStarterJava(starterResult);
                return starterResult;
            } else return null;
        });
    }

    public void updateWinners() {
        d = gamePref.getGamePreference().subscribeOn(Schedulers.io()).doOnNext( pref -> {
            List<int[]> tempWinnerLine = new ArrayList<>();
            for (String i: (pref.getWinnerLine())) {
                int[] winnerPos = CubeAdapter.getGridAdapter(i);
                Log.d(TAG, "updateWinners: " + winnerPos[0] + " " + winnerPos[1]);
                tempWinnerLine.add(winnerPos);
            }
            winnerLine.setValue(tempWinnerLine);
            gamePref.clearWinnerLineJava();
            dispose(d);
        }).subscribe();
    }

    public LiveData<List<int[]>> getWinnerLine() {
        return winnerLine;
    }

    public void clearWinnerLine() {
        winnerLine.setValue(null);
    }

    public void updateTurn(String turn) { gameRepository.updateTurn(turn); }

    public int setCubeMove(String playedPiece) {
        if (app.getString(R.string.circle).equals(playedPiece)) {
            crossTurn();
            return lastCircle;
        } else{
            circleTurn();
            return lastCross;
        }
    }

    public void setCubePos(int[] lastPosition, String lastPiece) {
        this.lastPosition = lastPosition;
        lastPiecePlayed = lastPiece.equals(app.getString(R.string.cross)) ? crossDrawable : circleDrawable;
    }

    public int[] getLastCube() { return lastPosition; }
    public int getLastPiecePlayed() { return lastPiecePlayed; }

    public void clearMoves() { moveRepository.deleteGameMoves(); }
}
