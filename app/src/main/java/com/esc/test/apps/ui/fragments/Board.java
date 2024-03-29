package com.esc.test.apps.ui.fragments;

import static com.esc.test.apps.common.adaptors.CubeAdapter.getGridAdapter;
import static com.esc.test.apps.common.utils.AlertType.WINNER;
import static com.esc.test.apps.domain.viewmodels.board.PlayAIViewModel.AI_GAME;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.esc.test.apps.R;
import com.esc.test.apps.common.adaptors.CubeAdapter;
import com.esc.test.apps.data.models.pojos.CubeID;
import com.esc.test.apps.databinding.BoardActivityBinding;
import com.esc.test.apps.common.utils.AlertType;
import com.esc.test.apps.domain.viewmodels.board.PassPlayBoardViewModel;
import com.esc.test.apps.domain.viewmodels.board.PlayAIViewModel;
import com.esc.test.apps.domain.viewmodels.board.PlayFriendBoardViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class Board extends Fragment {

    public Board() { super(R.layout.board_activity); }

    private final ArrayList<GridView> layers = new ArrayList<>();
    private PassPlayBoardViewModel passPlayViewModel;
    private PlayFriendBoardViewModel playFriendViewModel;
    private PlayAIViewModel playAIViewModel;
    private static final String TAG = "myT";
    private int numLayers;
    private BoardActivityBinding binding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = BoardActivityBinding.bind(view);
        Log.d("myT", "BoardActivity");
        passPlayViewModel = new ViewModelProvider(this).get(PassPlayBoardViewModel.class);
        setBoard();
        checkExtras();
        onBackPressed();
    }

    private void setBoard() {
        setPieceClickEnabled();
        passPlayViewModel.clearMoves();
        if (layers.isEmpty()) addLayers();
        numLayers = 0;
        layers.forEach(i -> {
            passPlayViewModel.setCubes(numLayers);
            CubeAdapter cubeAdapter = new CubeAdapter(requireContext(), passPlayViewModel.getLayerIDs().get(numLayers++));
            i.setAdapter(cubeAdapter);
            i.setOnItemClickListener((adapterView, view, j, l) -> changeSquareIcon(view));
        });
        setObservers();
    }

    private void addLayers() {
        layers.add(binding.front);
        layers.add(binding.frontMiddle);
        layers.add(binding.backMiddle);
        layers.add(binding.back);
    }

    private void checkExtras() {
        BoardArgs extras = BoardArgs.fromBundle(getArguments());

        if (extras.getGameType() != null) {
            if (!Objects.equals(extras.getGameType(), AI_GAME)) {
                gameButtonsVis();
                String gamePiece = extras.getGamePiece();
                String uids = extras.getGameType();
                playFriendViewModel = new ViewModelProvider(this).get(PlayFriendBoardViewModel.class);
                Log.d(TAG, "friend's starting piece is: " + gamePiece);
                if (Objects.equals(gamePiece, getResources().getString(R.string.cross))) {
                    playFriendViewModel.getGameUids(uids, true);
                    changeGridOnClick(false);
                } else {
                    playFriendViewModel.getGameUids(uids, false);
                    changeGridOnClick(true);
                }
                setOpponentUIDObserver();
            } else {
                if (extras.getGamePiece() != null) firstTimeInstructions();
                Log.d(TAG, "checkExtras: ai game");
                passPlayViewModel.clearLocalGame();
                playAIViewModel = new ViewModelProvider(this).get(PlayAIViewModel.class);
                binding.level.setVisibility(View.VISIBLE);
                setPlayAIObserver();
                getNewMoves();
            }
        } else {
            Log.d(TAG, "checkExtras: local game");
            setPassPlayObserver();
            getNewMoves();
            passPlayViewModel.clearLocalGame();
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
        CubeID cube = (CubeID) view.getTag();
        int confirmColor = ContextCompat.getColor(requireContext(), R.color.colorTransBlue);
        if (viewColor == null || viewColor.getColor() != confirmColor) {
            String lastPos = passPlayViewModel.getLastPos();
            if (lastPos != null) removeConfirm(lastPos);
            view.setBackgroundColor(confirmColor);
            passPlayViewModel.setLastPos(cube.getArrayPos());
        } else {
            passPlayViewModel.setLastPos(null);
            passPlayViewModel.updateView(cube);
            if (playFriendViewModel != null) playFriendViewModel.newMove(cube);
            else if (playAIViewModel != null) playAIViewModel.newMove(cube);
            else passPlayViewModel.newMove((CubeID) view.getTag());
        }
    }

    private void removeConfirm(String tag) {
        int[] turnPos = getGridAdapter(tag);
        layers.get(turnPos[0]).getChildAt(turnPos[1]).setBackground(null);
    }

    public void setButtonClicks() {
        binding.newGame.setOnClickListener(v -> {
            passPlayViewModel.clearLocalGame();
            if (playAIViewModel != null) playAIViewModel.newGame();
            changeGridOnClick(true);
            setBoard();
            setPieceClickEnabled();
        });
        binding.newSet.setOnClickListener(v -> {
            passPlayViewModel.clearSet();
            setBoard();
            changeGridOnClick(true);
            setPieceClickEnabled();
        });
        binding.level.setOnClickListener(this::levelPopup);
        binding.quit.setOnClickListener(v -> {
            if (playFriendViewModel != null)
                NavHostFragment.findNavController(this)
                    .navigate(BoardDirections.actionBoardActivityToAlertDialogFragment
                        (getString(
                            R.string.confirm_quit), getString(R.string.quit_msg), AlertType.QUIT));
        });
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
        passPlayViewModel.getCircleScore().observe(getViewLifecycleOwner(), s ->
                binding.oScore.setText(s));
        passPlayViewModel.getCrossScore().observe(getViewLifecycleOwner(), s ->
                binding.xScore.setText(s));
        passPlayViewModel.getoTurn().observe(getViewLifecycleOwner(), color ->
                binding.oButton.setBackgroundColor(requireContext().getColor(color)));
        passPlayViewModel.getxTurn().observe(getViewLifecycleOwner(), color ->
                binding.xButton.setBackgroundColor(requireContext().getColor(color)));
        passPlayViewModel.getStarter().observe(getViewLifecycleOwner(), s -> {
            if (s != null) gameStarted(); });
        passPlayViewModel.getWinner().observe(getViewLifecycleOwner(), winner -> {
            if (!winner.isEmpty()) {
                Log.d(TAG, "winner : " + winner);
                binding.title.setText(winner + getResources().getString(R.string.game_won));
                changeGridOnClick(false);
                if (playFriendViewModel != null) playFriendViewModel.uploadWinner();
            }
        });
        passPlayViewModel.getWinnerLine().observe(getViewLifecycleOwner(), winnerLine -> {
            if (winnerLine != null) {
                for (int[] winPos : winnerLine)
                    layers.get(winPos[0]).getChildAt(winPos[1])
                            .setBackground(requireContext().getDrawable(R.drawable.win_star));
                passPlayViewModel.clearWinnerLine();
            }
        });
        setButtonClicks();
    }

    private void setPassPlayObserver() {
        passPlayViewModel.getTurn().observe(getViewLifecycleOwner(), turn -> {
            if (!turn.isEmpty()) binding.title.setText(turn + "\'s turn");
        });
    }

    private void setPlayAIObserver() {
        playAIViewModel.getLastMove().observe(getViewLifecycleOwner(), move -> {
            if (!move.getPosition().isEmpty()) updateGridView(move.getPosition(), move.getPiecePlayed());
        });
        playAIViewModel.error.observe(getViewLifecycleOwner(), error -> Snackbar.make(
                binding.getRoot(), error, Snackbar.LENGTH_LONG).show());
    }

    private void setOpponentUIDObserver() {
        playFriendViewModel.getTurn().observe(getViewLifecycleOwner(), turn -> {
            if (!turn.getTurn().isEmpty()) {
                if (turn.isFriendsTurn()) {
                    binding.title.setText(getResources().getString(R.string.their_turn));
                    changeGridOnClick(false);
                } else {
                    binding.title.setText(getResources().getString(R.string.your_turn));
                    changeGridOnClick(true);
                }
            }
        });
        playFriendViewModel.existingMoves.observe(getViewLifecycleOwner(), s -> {
            if (s != null) {
                if (!s.isEmpty()) {
                    playFriendViewModel.addExistingMoves(s);
                    s.forEach(move -> updateGridView(move.getPosition(), move.getPiecePlayed()));
                }
            }
        });
        playFriendViewModel.movesReady.observe(getViewLifecycleOwner(), ready -> {
            if (ready) observeFriendMoves();
        });
        playFriendViewModel.winReady.observe(getViewLifecycleOwner(), ready -> {
            if (ready) observeGameState();
        });
        playFriendViewModel.network.observe(getViewLifecycleOwner(), s -> {
            if (s != null)
                if (s) {
                    changeGridOnClick(true);
                    Snackbar.make(
                            binding.getRoot(), "Connection restored", Snackbar.LENGTH_LONG).show();
                } else {
                    changeGridOnClick(false);
                    Snackbar.make(
                        binding.getRoot(), "No network connection", Snackbar.LENGTH_INDEFINITE).show();
                }
        });
        getNewMoves();
    }

    private void observeFriendMoves() {
        playFriendViewModel.moveInfo.observe(getViewLifecycleOwner(), s -> {
            if (s != null) {
                updateGridView(s.getPosition(), s.getPiecePlayed());
                passPlayViewModel.downloadedMove(s);
                Log.d(TAG, "downloaded move view update: ");
            }
        });
    }

    private void observeGameState() {
        playFriendViewModel.winState.observe(getViewLifecycleOwner(), winnerInfo -> {
            if (!winnerInfo.get("winner").isEmpty()) {
                NavHostFragment.findNavController(this)
                        .navigate(BoardDirections.actionBoardActivityToAlertDialogFragment(
                                winnerInfo.get("player") + " won",
                                "Start another game in the friends page", WINNER));
//                playFriendViewModel.winState.removeObservers(getViewLifecycleOwner());
            }
        });
    }

    private void getNewMoves() {
        passPlayViewModel.lastMove.observe(getViewLifecycleOwner(), s -> {
            if (s != null) updateGridView(s.getPos(), s.getPiece());
        });
    }

    private void updateGridView(String pos, String playedPiece) {
        int[] turnPos = getGridAdapter(pos), lastTurn = passPlayViewModel.getLastCube();
        int lastDrawable = passPlayViewModel.getLastPiecePlayed();
        if (lastDrawable != 0) layers.get(lastTurn[0]).getChildAt(lastTurn[1])
                                    .setBackground(requireContext().getDrawable(lastDrawable));
//        Log.d(TAG, "updated view " + turnPos[0] + " " + turnPos[1] + " " + playedPiece);
        layers.get(turnPos[0]).getChildAt(turnPos[1])
                .setBackground(requireContext().getDrawable(passPlayViewModel.setCubeMove(playedPiece)));
        layers.get(turnPos[0]).getChildAt(turnPos[1]).setOnClickListener(null);
        passPlayViewModel.setCubePos(turnPos, playedPiece);
    }

    private void levelPopup(View v) {
        PopupMenu popup = new PopupMenu(requireContext(), v);
        popup.getMenuInflater().inflate(R.menu.levels, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            playAIViewModel.setLevel(item.getTitle());
            Snackbar.make(binding.getRoot(),
                    "Difficulty will change when you start a new game", Snackbar.LENGTH_SHORT).show();
            return true;
        });
        popup.show();
    }

    private void firstTimeInstructions() {
        levelPopup(binding.level);
    }

    private void onBackPressed() {
        requireActivity().getOnBackPressedDispatcher()
            .addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    if (playFriendViewModel != null)
                        NavHostFragment.findNavController(Board.this)
                                .navigate(BoardDirections.actionBoardActivityToAlertDialogFragment
                                        (getString(
                                                R.string.confirm_quit), getString(R.string.quit_msg), AlertType.QUIT));
                    else NavHostFragment.findNavController(Board.this)
                            .navigate(BoardDirections.actionBoardActivityToHome());
                }
            }
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        passPlayViewModel.clearMoves();
        Log.d("myT", "destroy");
    }

}
