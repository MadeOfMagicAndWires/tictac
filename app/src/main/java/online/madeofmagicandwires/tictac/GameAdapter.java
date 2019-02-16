package online.madeofmagicandwires.tictac;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GameAdapter extends RecyclerView.Adapter<GameViewHolder> implements View.OnClickListener {

    /** the default tile layout file **/
    public static final @LayoutRes int DEFAULT_LAYOUT = R.layout.gametile;


    private final int TILE_SIZE;

    /** game instance used to determine board state **/
    private Game mGame;
    /** inflates the layout file to be used **/
    private LayoutInflater inflater;
    /** layout file to be inflated **/
    private @LayoutRes int layout;


    /**
     * Most specific constructor
     * @param c Activity layout
     * @param game game instance representing the current gamestate
     * @param layoutFile layout file to be used
     * @param parentWidth the total width of the parent (Recycler)view
     */
    public GameAdapter(@NonNull Context c, @NonNull Game game, @LayoutRes int layoutFile, int parentWidth) {
        this.mGame = game;
        this.inflater = LayoutInflater.from(c);
        this.layout = layoutFile;
        this.TILE_SIZE = (int) Math.floor(parentWidth / (double) game.boardSize);
    }

    /**
     * Constructor leaving out the total pixel size of the parent (Recycler)View;
     * will use {@link R.dimen#board_grid_min_size} as default fallback.
     * @param c Context needed to retrieve a LayoutInflator
     * @param game game instance representing the current gamestate
     * @param layoutFile layout file to be used
     */
    public GameAdapter(@NonNull Context c, @NonNull Game game, @LayoutRes int layoutFile) {
        this(c, game, layoutFile, c.getResources().getDimensionPixelSize(R.dimen.board_grid_min_size));

    }

    /**
     * Constructor leaving out the layout xml and total pixel size of the parent (Recycler)View;
     * will use {@link #DEFAULT_LAYOUT} and {@link R.dimen#board_grid_min_size} as default fallback
     * @param c Context needed to retrieve a LayoutInflator
     * @param game game instance representing the current gamestate
     */
    public GameAdapter(@NonNull Context c, @NonNull Game game) {
        this(c, game, DEFAULT_LAYOUT, c.getResources().getDimensionPixelSize(R.dimen.board_grid_min_size));
    }


    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = inflater.inflate(layout, parent, false);
        Point coords = getCoordinatesFromAdapterPosition(i);
        return new GameViewHolder(v, mGame.getTile(coords.x, coords.y));
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder viewHolder, int i) {
        // set the size of the tile based on how big the board is
        int calculatedTileSize = (TILE_SIZE > viewHolder.tile.getMinWidth()) ? TILE_SIZE : viewHolder.tile.getMinWidth();
        viewHolder.tile.setHeight(calculatedTileSize);
        viewHolder.tile.setWidth(calculatedTileSize);

        // retrieve tile coordinates
        Point coords = getCoordinatesFromAdapterPosition(i);


        // set data according to tile
        viewHolder.setState(mGame.getTile(coords.y, coords.x));
        viewHolder.colourTile(mGame.getGameOver());

        // finally set the coordinates as tag
        viewHolder.tile.setTag(R.id.coordinates, coords);

        if(mGame.getGameOver() == GameState.IN_PROGRESS) {
            viewHolder.tile.setOnClickListener(this);
        } else {
            viewHolder.tile.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return (mGame.boardSize * mGame.boardSize);
    }

    /**
     * Retrieves the item's 2D grid coordinates based on its 1D position (assuming the grid has the same boardSize as mGame)
     * @param pos position
     * @see #mGame
     * @return the item's grid coordinates held in a Point (for convenience's sake)
     */
    private Point getCoordinatesFromAdapterPosition(int pos) {
        int x = (int) Math.floor((pos/(double) mGame.boardSize));
        int y = (pos % mGame.boardSize);
        return new Point(x, y);
    }

    public void resetBoard(){
        mGame.resetBoard();
        notifyDataSetChanged();
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        Point coords = (Point) v.getTag(R.id.coordinates);
        if(coords != null) {
            TileState move = mGame.choose(coords.y, coords.x);
            if(move != TileState.INVALID) {
                Log.d("OnClick", move.toString() + " played move " +  (coords.y + 1) + "-" + (coords.x + 1));
                v.setClickable(false);

                // check if a win condition has been reached.
                GameState hasWon = mGame.checkWinconditionReached(coords.y, coords.x);

                // Handle results; show the win or move on to the next move.
                switch (hasWon) {
                    case IN_PROGRESS:
                        // after move admin; increase played moves, next player etc.
                        mGame.nextMove();
                        break;
                    case PLAYER_ONE_WIN:
                        showWin(v, hasWon);
                        break;
                    case PLAYER_TWO_WIN:
                        showWin(v, hasWon);
                        break;
                    case DRAW:
                        showWin(v, hasWon);
                        break;
                }

                // update adapter
                notifyDataSetChanged();

            }
        }
    }

    /**
     * Visually shows who won the game
     * @param v
     * @param gs
     */
    private void showWin(View v, @NonNull GameState gs) {
        Log.d("Game Over!", gs.toString());
        Snackbar snackbar = Snackbar.make(v, gs.toString(), Snackbar.LENGTH_LONG).
                setAction("RESET",
                        new View.OnClickListener(){
                            @Override public void onClick(View view) {
                                resetBoard();
                            }});

        // actually show snackbar
        snackbar.show();
    }

}
