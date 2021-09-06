package com.esc.test.apps.activities;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.esc.test.apps.R;
import com.esc.test.apps.adapters.CubeAdapter;
import com.esc.test.apps.databinding.ActivityMainBinding;
import com.esc.test.apps.gamestuff.CubeID;
import com.esc.test.apps.modelviews.PassPlayBoardViewModel;
import com.esc.test.apps.modelviews.PlayFriendBoardViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BoardActivity extends AppCompatActivity implements View.OnClickListener {

    private final ArrayList<GridView> layers = new ArrayList<>();
    private PassPlayBoardViewModel passPlayViewModel;
    private PlayFriendBoardViewModel playWithFriendViewModel;
    private static final String TAG = "myT";
    private int numLayers;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d("myT", "BoardActivity");
        passPlayViewModel = new ViewModelProvider(this).get(PassPlayBoardViewModel.class);
        setBoard();
        checkExtras();
    }

    private void setBoard() {
        setPieceClickEnabled();
        if (layers.isEmpty()) addLayers();
        numLayers = 0;
        layers.forEach(i -> {
            passPlayViewModel.setCubes(numLayers);
            CubeAdapter cubeAdapter = new CubeAdapter(getApplication(), passPlayViewModel.getLayerIDs().get(numLayers));
            i.setAdapter(cubeAdapter);
            i.setOnItemClickListener((adapterView, view, j, l) -> changeSquareIcon(view));
            numLayers++;
        });
        passPlayViewModel.setEnded(false);
        setObservers();
    }

    private void addLayers() {
        layers.add(binding.front);
        layers.add(binding.frontMiddle);
        layers.add(binding.backMiddle);
        layers.add(binding.back);
    }

    private void checkExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            passPlayViewModel.clearMoves();
            gameButtonsVis();
            String extra = (String) extras.get("friend_game_piece");
            playWithFriendViewModel = new ViewModelProvider(this).get(PlayFriendBoardViewModel.class);
            Log.d(TAG, "friend's starting piece is: " + extra);
            String uids = (String) extras.get("game_set_id");
            if (extra.equals(getResources().getString(R.string.opponent))) {
                playWithFriendViewModel.getGameUID(uids, true);
                changeGridOnClick(false);
                Log.d(TAG, "disable click: ");
            } else {
                playWithFriendViewModel.getGameUID(uids, false);
                changeGridOnClick(true);
                Log.d(TAG, "enable click: ");
            }
            setOpponentUIDObserver();
        } else {
            setPassPlayObserver();
            getNewMoves();
        }
    }

    private void changeGridOnClick(boolean canClick) {
        layers.forEach(g -> g.setEnabled(canClick));
    }

    private void gameButtonsVis() {
        binding.newGame.setVisibility(View.GONE);
        binding.newSet.setVisibility(View.GONE);
        binding.xScore.setVisibility(View.GONE);
        binding.oScore.setVisibility(View.GONE);
        binding.quit.setVisibility(View.VISIBLE);
    }

    private void changeSquareIcon(View view) {
        ColorDrawable viewColor = (ColorDrawable) view.getBackground();
        int confirmColor = ContextCompat.getColor(this, R.color.colorTransBlue);
        if (viewColor == null || viewColor.getColor() != confirmColor) {
            String lastPos = passPlayViewModel.getLastPos();
            if (lastPos != null) removeConfirm(lastPos);
            view.setBackgroundColor(confirmColor);
            CubeID cube = (CubeID) view.getTag();
            passPlayViewModel.setLastPos(cube.getArrayPos());
        } else {
            passPlayViewModel.setLastPos(null);
            passPlayViewModel.newMove((CubeID) view.getTag());
            Log.d(TAG, "changeSquareIcon: " + ((CubeID) view.getTag()).getArrayPos());
        }
    }

    private void removeConfirm(String tag) {
        int[] turnPos = CubeAdapter.getGridAdapter(tag);
        layers.get(turnPos[0]).getChildAt(turnPos[1]).setBackground(null);
    }

    @Override
    public void onClick(View view) {
        if (view == binding.newGame) {
            passPlayViewModel.setEnded(false);
            passPlayViewModel.clearGame();
            changeGridOnClick(true);
            setBoard();
            setPieceClickEnabled();
        } else if (view == binding.newSet) {
            passPlayViewModel.setEnded(false);
            passPlayViewModel.clearSet();
            setBoard();
            changeGridOnClick(true);
            setPieceClickEnabled();
        } else if (view == binding.quit) {
            playWithFriendViewModel.endGame(null);
        }
    }

    private void setPieceClickEnabled() {
        binding.xButton.setOnClickListener(view -> {
            passPlayViewModel.updateTurn(getResources().getString(R.string.cross));
            passPlayViewModel.crossTurn();
        });
        binding.oButton.setOnClickListener(view -> {
            passPlayViewModel.updateTurn(getResources().getString(R.string.circle));
            passPlayViewModel.circleTurn();
        });
    }

    private void gameStarted() {
        binding.xButton.setOnClickListener(null);
        binding.oButton.setOnClickListener(null);
    }

    private void setObservers() {
        passPlayViewModel.getCircleScore().observe(this, s -> binding.oScore.setText(s));
        passPlayViewModel.getCrossScore().observe(this, s -> binding.xScore.setText(s));
        passPlayViewModel.getoTurn().observe(this, color -> binding.oButton.setBackgroundColor(getColor(color)));
        passPlayViewModel.getxTurn().observe(this, color -> binding.xButton.setBackgroundColor(getColor(color)));
        passPlayViewModel.getStarter().observe(this, s -> { if (s != null) gameStarted(); });
        passPlayViewModel.getWinner().observe(this, winner -> {
            if (winner != null) {
                Log.d(TAG, "winner : " + winner);
                passPlayViewModel.updateScore(winner);
                binding.title.setText(winner + getResources().getString(R.string.game_won));
                passPlayViewModel.updateWinners();
                changeGridOnClick(false);
                if (playWithFriendViewModel != null) playWithFriendViewModel.uploadWinner();
            }
        });
        passPlayViewModel.getWinnerLine().observe(this, winnerLine -> {
            if (winnerLine != null) {
                for (int[] winPos : winnerLine)
                    layers.get(winPos[0]).getChildAt(winPos[1])
                            .setBackground(getDrawable(R.drawable.baseline_star_24));
                passPlayViewModel.clearWinnerLine();
            }
        });
    }

    private void setPassPlayObserver() {
        passPlayViewModel.getTurn().observe(this, turn -> {
            if (turn != null) binding.title.setText(turn + "\'s turn");
        });
    }

    private void setOpponentUIDObserver() {
        playWithFriendViewModel.getTurn().observe(this, turn -> {
            if (turn != null) {
                if (turn.isFriendsTurn()) {
                    binding.title.setText(getResources().getString(R.string.their_turn));
                    Log.d(TAG, "setPlayObservers: friend's turn");
                    changeGridOnClick(false);
                } else {
                    binding.title.setText(getResources().getString(R.string.your_turn));
                    Log.d(TAG, "setPlayObservers: your turn");
                    changeGridOnClick(true);
                }
            }
        });
        playWithFriendViewModel.getExistingMoves().observe(this, s -> {
            Log.d(TAG, "setOpponentUIDObserver: get moves");
            if (s != null) {
                if (!s.isEmpty()) {
                    playWithFriendViewModel.addExistingMoves(s);
                    s.forEach(move -> updateGridView(move.getPosition(), move.getPiece_played()));
                }
                getNewMoves();
                setPlayFriendObservers();
            }
        });
    }

    private void getNewMoves() {
        passPlayViewModel.getLastMove().observe(this, s -> {
            if (s != null) {
                binding.checkMove.setText(s.getPosition());
                updateGridView(s.getPosition(), s.getPiece_played());
            }
        });
    }

    private void setPlayFriendObservers() {
        playWithFriendViewModel.getMoveInfo().observe(this, s -> {
            if (s != null) passPlayViewModel.newFriendMove(s);
        });
    }

    private void updateGridView(String pos, String playedPiece) {
        int[] turnPos = CubeAdapter.getGridAdapter(pos);
        Log.d(TAG, "after " + turnPos[0] + " " + turnPos[1]);
        layers.get(turnPos[0]).getChildAt(turnPos[1]).setBackground(getDrawable(passPlayViewModel.setCubeMove(playedPiece)));
        layers.get(turnPos[0]).getChildAt(turnPos[1]).setOnClickListener(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        passPlayViewModel.clearMoves();
        Log.d("myT", "destroy");
    }
}
