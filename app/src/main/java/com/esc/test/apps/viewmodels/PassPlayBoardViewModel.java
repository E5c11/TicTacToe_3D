package com.esc.test.apps.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.esc.test.apps.R;
import com.esc.test.apps.adapters.CubeAdapter;
import com.esc.test.apps.datastore.GameState;
import com.esc.test.apps.entities.Game;
import com.esc.test.apps.entities.Move;
import com.esc.test.apps.pojos.CubeID;
import com.esc.test.apps.other.MovesFactory;
import com.esc.test.apps.repositories.GameRepository;
import com.esc.test.apps.repositories.MoveRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@HiltViewModel
public class PassPlayBoardViewModel extends ViewModel {

    private final ArrayList<CubeID[]> layerIDs = new ArrayList<>();
    private String lastPos;
    private final MutableLiveData<String> circleScore = new MutableLiveData<>();
    private final MutableLiveData<String> crossScore = new MutableLiveData<>();
    private final MutableLiveData<Integer> xTurn = new MutableLiveData<>();
    private final MutableLiveData<Integer> oTurn = new MutableLiveData<>();
    private final MutableLiveData<Boolean> ended = new MutableLiveData<>();
    private final MutableLiveData<List<int[]>> winnerLine = new MutableLiveData<>();
    private final LiveData<String> winner;
    private final LiveData<String> starter;
    private final LiveData<Move> lastMove;
    private final GameState gameState;
    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;
    private final MovesFactory moves;
    private static final String TAG = "myT";
    private final int turnColor, notTurnColor;
    private final int crossDrawable, circleDrawable;
    private final Application app;
    private Disposable d;

    @Inject
    public PassPlayBoardViewModel(MovesFactory moves, GameState gameState, MoveRepository moveRepository,
                                  GameRepository gameRepository, Application app
    ) {
        populateGridLists();
        turnColor = R.color.colorAccent;
        notTurnColor = R.color.colorPrimary;
        circleDrawable = R.drawable.baseline_panorama_fish_eye_24;
        crossDrawable = R.drawable.baseline_close_24;
        this.moves = moves;
        this.gameState = gameState;
        this.gameRepository = gameRepository;
        this.moveRepository = moveRepository;
        this.app = app;
        setBeforeGame();
        winner = gameRepository.getWinner();
        starter = moveRepository.getFirstMove();
        lastMove = moveRepository.getLastMove();
    }

    private void populateGridLists() {
        CubeID[] frontCubes = new CubeID[16];
        layerIDs.add(frontCubes);
        CubeID[] frontMCubes = new CubeID[16];
        layerIDs.add(frontMCubes);
        CubeID[] backMCubes = new CubeID[16];
        layerIDs.add(backMCubes);
        CubeID[] backCubes = new CubeID[16];
        layerIDs.add(backCubes);
    }

    public ArrayList<CubeID[]> getLayerIDs() {return layerIDs;}

    public void setCubes(int z) {
        int x = 0, y = 0;
        CubeID[] cubes = layerIDs.get(z);
        for (int i = 0; i < cubes.length; i++) {
            String cubeName = x + "" + y + "" + z;
            String cubePos = String.valueOf((x * 4) + y + (z * 16));
            if (y == 0 || y == 1 || y == 2) {
                y++;
                cubes[i] = new CubeID(cubeName, cubePos);
            } else {
                cubes[i] = new CubeID(cubeName, cubePos);
                x++;
                y = 0;
            }
        }
    }

    public void clearSet() {
        gameState.newSet();
        clearGame();
        circleScore.setValue("0"); crossScore.setValue("0");
    }

    public void clearGame() {
        gameState.newGame();
        insertNewGame();
        moveRepository.deleteGameMoves();
        if (gameRepository.getPreviousStarter().toString().equals(app.getString(R.string.circle))) circleTurn();
        else crossTurn();
    }

    private void insertNewGame() {
        gameRepository.insertGame(new Game("in progress", "cross", "", "opponent", "not started"));
    }

    public void addCircleScore() {
        if (circleScore.getValue() == null) circleScore.setValue("0");
        else circleScore.setValue(String.valueOf(Integer.parseInt(circleScore.getValue()) + 1));
        gameState.setCircleScore(circleScore.getValue());
    }
    public MutableLiveData<String> getCircleScore() {return circleScore;}

    public void addCrossScore() {
        if (crossScore.getValue() == null) crossScore.setValue("0");
        else crossScore.setValue(String.valueOf(Integer.parseInt(crossScore.getValue()) + 1));
        gameState.setCrossScore(crossScore.getValue());
    }
    public MutableLiveData<String> getCrossScore() {return crossScore;}

    public void setBeforeGame() {
        clearGame();
        circleScore.setValue(gameState.getCircleScore());
        crossScore.setValue(gameState.getCrossScore());
    }

    public void setLastPos(String tag) { lastPos = tag; }
    public String getLastPos() { return lastPos; }

    public void newMove(CubeID cubeID) {
        d = gameRepository.getTurn().subscribeOn(Schedulers.io()).doOnNext(t -> {
            if (gameState.getStarter() == null) {
                gameState.setStarter(t);
                gameRepository.setStarter(t);
            }
            Log.d(TAG, "newMove 2: " + t);
            moves.createMoves(cubeID.getCoordinates(), t);
            dispose();
        }).subscribe();
    }

    private void dispose() {
        d.dispose();
    }

    public void newFriendMove(Move move) {
        Log.d(TAG, "newFriendMove: add move to db " + move.getPiece_played());
        if (lastMove.getValue() == null) downloadedMove(move);
        else if (!move.getPosition().equals(lastMove.getValue().getPosition())) downloadedMove(move);
    }

    private void downloadedMove(Move move) {
        moves.createMoves(String.valueOf(move.getCoordinates()), String.valueOf(move.getPiece_played()));
        if (String.valueOf(move.getPiece_played()).equals(app.getString(R.string.cross))) circleTurn();
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

    public void setEnded(boolean end) { ended.setValue(end); }

    public LiveData<Move> getLastMove() {return lastMove;}

    public LiveData<Integer> getxTurn() { return xTurn; }

    public LiveData<Integer> getoTurn() { return oTurn; }

    public LiveData<String> getTurn() {
        return Transformations.map(getTurnSource(), turnResult -> {
            if (gameState.isWinner() == null) return turnResult;
            else return null;
        });
    }

    private LiveData<String> getTurnSource() {
        return LiveDataReactiveStreams
                .fromPublisher(gameRepository.getTurn().subscribeOn(Schedulers.io()));
    }

    public LiveData<String> getWinner() {
        return Transformations.map(winner, winResult -> {
            if (winResult != null && !winResult.equals("in progress") && gameState.isWinner() != null)
                return winResult;
            else return null;
        });
    }

    public LiveData<String> getStarter() {
        return Transformations.map(starter, starterResult -> {
            if (starterResult != null) {
                gameState.setStarter(starterResult);
                return starterResult;
            } else return null;
        });
    }

    public void updateWinners() {
        List<int[]> tempWinnerLine = new ArrayList<>();
        for (String i: gameState.getWinnerLine()) {
            int[] winnerPos = CubeAdapter.getGridAdapter(i);
            Log.d(TAG, "updateWinners: " + winnerPos[0] + " " + winnerPos[1]);
            tempWinnerLine.add(winnerPos);
        }
        winnerLine.setValue(tempWinnerLine);
        gameState.clearWinnerLine();
    }

    public LiveData<List<int[]>> getWinnerLine() {
        return winnerLine;
    }

    public void clearWinnerLine() {
        winnerLine.setValue(null);
    }

    public void updateTurn(String turn) {gameRepository.updateTurn(turn);}

    public int setCubeMove(String playedPiece) {
        if (app.getString(R.string.circle).equals(playedPiece)) {
            crossTurn();
            return circleDrawable;
        } else{
            circleTurn();
            return crossDrawable;
        }
    }

    public void clearMoves() {
        moveRepository.deleteGameMoves();
        gameState.setGameID(null);
        gameState.setGameSetID(null);
    }

}
