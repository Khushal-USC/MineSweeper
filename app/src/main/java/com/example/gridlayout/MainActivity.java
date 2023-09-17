package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
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

    String mineString;
    String flagString;

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cell_tvs = new ArrayList<TextView>();
        mineSet = new HashSet<TextView>();
        mineString = getString(R.string.mine);
        flagString = getString(R.string.flag);


        // Method (2): add four dynamically created cells
        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);
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

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.setMargins(dpToPixel(2), dpToPixel(2), dpToPixel(2), dpToPixel(2));
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);

                grid.addView(tv, lp);
                //tv.setText(cell_tvs.size() + "");
                tv.setId(cell_tvs.size());
                cell_tvs.add(tv);
            }
        }
        generateMines(10);
    }

    private int findIndexOfCellTextView(TextView tv) {
        for (int n=0; n<cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv)
                return n;
        }
        return -1;
    }

    public void onClickTV(View view){
        TextView tv = (TextView) view;
        List<TextView> adj = getAdjacentTVs(tv);
        revealCell(tv);
//        for(TextView x : adj){
//            flipCellColor(x);
//        }
//        int n = findIndexOfCellTextView(tv);
//        int i = n/COLUMN_COUNT;
//        int j = n%COLUMN_COUNT;
        //tv.setText(String.valueOf(i)+String.valueOf(j));

    }

    private void flipCellColor(TextView tv){
//        if (tv.getCurrentTextColor() == Color.GRAY) {
//            tv.setTextColor(Color.GREEN);
//            tv.setBackgroundColor(Color.parseColor("lime"));
//        } else {
//            tv.setTextColor(Color.GRAY);
//            tv.setBackgroundColor(Color.LTGRAY);
//        }
        //if (tv.getCurrentTextColor() == Color.GRAY) {
            tv.setBackgroundColor(Color.parseColor("lime"));
        //}
    }
    //Empties set then populates with 4 mines
    public void generateMines(int mineCount){
        mineSet.clear();
        while(mineSet.size() < mineCount){
            int randomIndex = (int) (Math.random() * cell_tvs.size());
            TextView tv = cell_tvs.get(randomIndex);
            if(!mineSet.contains(tv)){
                tv.setText(mineString);
                for(TextView u : getAdjacentTVs(tv)){
                    String s = u.getText().toString();
                    if(!(s.equals(mineString) || s.equals(flagString))) {
                        incrementCellScore(u);
                    }
                }
                mineSet.add(tv);
            }
        }
    }

    public void incrementCellScore(TextView tv){
        int currCellScore = stoi(tv.getText().toString());
        tv.setText((currCellScore+1) + "");
    }

    public void revealCell(TextView tv){
        if(tv.getCurrentTextColor() == Color.GREEN){
            tv.setTextColor(Color.BLACK);
            tv.setBackgroundColor(Color.GRAY);
        }
        for(TextView x : getAdjacentTVs(tv)){
            if(x.getText().toString().equals("") && x.getCurrentTextColor() == Color.GREEN){
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
                System.out.println(x.x + " : " + x.y);
                res.add(cell_tvs.get(getI(x.x,x.y)));
            }
        }

        return res;
    }
}