package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int ROW_COUNT = 12;
    private static final int COLUMN_COUNT = 10;

    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private ArrayList<TextView> cell_tvs;
    private Set<TextView> mineSet;
    final Handler handler = new Handler();

    boolean isPick = true;

    int seconds = 0;
    boolean running = true;
    boolean gameEnd = false;
    boolean won = false;

    String mineString;
    String flagString;
    String[] buttonOptions;

    TextView flagCount;

    String pickString;
    int mineCount = 4;

    int cellsRevealed = 0;

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initGame();
        runTimer();

    }

    private void initGame(){
        running = true;
        gameEnd = false;
        seconds = 0;
        cellsRevealed = 0;
        cell_tvs = new ArrayList<TextView>();
        mineSet = new HashSet<TextView>();
        mineString = getString(R.string.mine);
        flagString = getString(R.string.flag);
        pickString = getString(R.string.pick);
        buttonOptions = new String[]{pickString, flagString};
        flagCount = (TextView) findViewById(R.id.textView4);

        View parentView = findViewById(R.id.parentView);

        parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gameEnd == true){
                    gameEnd = false;
                    revealEndScreen(won);
                }
            }
        });

        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);
        TextView button = (TextView) findViewById(R.id.textView2);
        button.setOnClickListener(this::onClickTV);
        button.setTag("B");

        for (int i = 0; i<=11; i++) {
            for (int j=0; j<=9; j++) {
                TextView tv = new TextView(this);
                tv.setHeight( dpToPixel(34) );
                tv.setWidth( dpToPixel(34) );
                tv.setTextSize( 20 ); //dpToPixel(32)
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setTextColor(Color.GREEN);
                tv.setBackgroundColor(Color.GREEN);
                tv.setOnClickListener(this::onClickTV);
                tv.setTag("NB");
                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.setMargins(dpToPixel(2), dpToPixel(2), dpToPixel(2), dpToPixel(2));
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);
                grid.addView(tv, lp);
                tv.setId(cell_tvs.size());
                cell_tvs.add(tv);
            }
        }
        generateMines(mineCount);
    }

    private int findIndexOfCellTextView(TextView tv) {
        for (int n=0; n<cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv)
                return n;
        }
        return -1;
    }

    private void runTimer() {
        final TextView timeView = (TextView) findViewById(R.id.textView5);
        handler.post (new Runnable() {
            @Override
            public void run() {
                int secs = seconds;
                String time = String.format("%03d", secs);
                timeView.setText(time);
                if (running) {
                    seconds++;
                }
                handler.postDelayed (this, 1000);
            }
        });
    }

    public void onClickTV(View view){
        TextView tv = (TextView) view;
        List<TextView> adj = getAdjacentTVs(tv);
        if(gameEnd == true){
            gameEnd = false;
            revealEndScreen(won);
            return;
        }
        if (tv.getTag().equals("B")){
            isPick = !isPick;
            tv.setText(buttonOptions[isPick ? 0 : 1]);
            return;
        }
        if(isPick){
            if(tv.getText().toString().contains(flagString)) {
                return;
            }
            if(tv.getTag().equals("BB")){
                running = false;
                gameEnd = true;
                revealBombs();
                won = false;
            }
        } else {
            if(tv.getText().toString().contains(flagString)){
                tv.setText(tv.getText().toString().substring(2,tv.getText().length()));
                incrementCellScore(flagCount);
            } else if(tv.getCurrentTextColor() == Color.GREEN && stoi(flagCount.getText().toString()) > 0){
                tv.setText(flagString+tv.getText());
                decrementCellScore(flagCount);
            }
            return;
        }
        revealCell(tv);
        if(cellsRevealed >= 120-mineCount)
        {
            running = false;
            revealBombs();
            gameEnd = true;
            won = true;
        }


    }

    private void flipCellColor(TextView tv){
        tv.setBackgroundColor(Color.parseColor("lime"));
    }

    private void revealEndScreen(boolean won){
        handler.removeCallbacksAndMessages(null); // Stop the timer
        setContentView(R.layout.end_screen);
        Button restartButton = findViewById(R.id.button);

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_main);
                initGame();
                runTimer();
            }
        });
        String x = won ? "won. Good Job!" : "lost. Try Again!";
        TextView winMessageTextView = findViewById(R.id.winMessageTextView);

        winMessageTextView.setText("Used " + seconds + " seconds. You "+x);

    }

    private void revealBombs(){
        for(TextView tv : mineSet){
            tv.setText(mineString);

            int red = (int) (Math.random() * 255);
            int green = (int) (Math.random() * 255);
            int blue = (int) (Math.random() * 255);

            tv.setBackgroundColor(Color.rgb(red, green,blue));
        }
    }

    //Empties set then populates with 4 mines
    public void generateMines(int mineCount){
        mineSet.clear();
        while(mineSet.size() < mineCount){
            int randomIndex = (int) (Math.random() * cell_tvs.size());
            TextView tv = cell_tvs.get(randomIndex);
            if(!mineSet.contains(tv)){
                tv.setText("B");
                tv.setTag("BB");
                for(TextView u : getAdjacentTVs(tv)){
                    String s = u.getText().toString();
                    if(!(u.getTag().equals("BB") || s.equals(flagString))) {
                        incrementCellScore(u);
                    }
                }
                mineSet.add(tv);
            }
        }
    }

    public void incrementCellScore(TextView tv){
        int currCellScore = stoi(tv.getText().toString());
        tv.setText(new StringBuilder().append(currCellScore + 1).append("").toString());
    }

    public void decrementCellScore(TextView tv){
        int currCellScore = stoi(tv.getText().toString());
        tv.setText(new StringBuilder().append(currCellScore - 1).append("").toString());
    }

    public void revealCell(TextView tv){
        cellsRevealed++;
        if(tv.getCurrentTextColor() == Color.GREEN){
            if(tv.getText().toString().equals(flagString)){
                tv.setText("");
            }
            tv.setTextColor(Color.BLACK);
            tv.setBackgroundColor(Color.GRAY);
        }

        for(TextView x : getAdjacentTVs(tv)){
            if(x.getCurrentTextColor() == Color.GREEN
                    && (x.getText().toString().equals("") ||
                    x.getText().toString().equals(flagString))){
                revealCell(x);
            }
        }
    }

    private int stoi(String s){
        if(s.equals("")){
            return 0;
        }
        return Integer.parseInt(s);
    }

    private boolean isInRange(int number, int min, int max) {
        return number >= min && number <= max;
    }

    int getI(int x, int y)
    {
        return (x) + (y)*COLUMN_COUNT;
    }

    Coordinate ItoXY(int i)
    {
        int x = i % COLUMN_COUNT;
        int y = i / COLUMN_COUNT;
        return new Coordinate(x,y);
    }

    public boolean isCoordValid(Coordinate coordinate) {
        return isInRange(coordinate.x, 0, COLUMN_COUNT-1) &&
                isInRange(coordinate.y, 0, ROW_COUNT-1);
    }

    public List<TextView> getAdjacentTVs(TextView tv){
        List<TextView> res = new ArrayList<TextView>();
        List<Coordinate> coordinateList = new ArrayList<>();
        int tvIndex = tv.getId();
        Coordinate tvCoords = ItoXY(tvIndex);
        coordinateList.add(new Coordinate(tvCoords.x, tvCoords.y + 1));     // top
        coordinateList.add(new Coordinate(tvCoords.x, tvCoords.y - 1));     // bottom
        coordinateList.add(new Coordinate(tvCoords.x - 1, tvCoords.y));     // left
        coordinateList.add(new Coordinate(tvCoords.x + 1, tvCoords.y));     // right
        coordinateList.add(new Coordinate(tvCoords.x - 1, tvCoords.y + 1)); // top_left
        coordinateList.add(new Coordinate(tvCoords.x - 1, tvCoords.y - 1)); // bottom_left
        coordinateList.add(new Coordinate(tvCoords.x + 1, tvCoords.y + 1)); // top_right
        coordinateList.add(new Coordinate(tvCoords.x + 1, tvCoords.y - 1)); // bottom_right

        for(Coordinate x : coordinateList){
            if(isCoordValid(x)){
                res.add(cell_tvs.get(getI(x.x,x.y)));
            }
        }

        return res;
    }
}