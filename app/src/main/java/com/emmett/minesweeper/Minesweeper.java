package com.emmett.minesweeper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

class Minesweeper implements Parcelable {
    private int column;
    private int row;
    private int size;
    private int mineNumber;
    private int countDown;
    private int boomPosition;
    private int[][] map;
    private boolean isMapCreated;
    private boolean isGameDone;
    private boolean[] isOpen;
    private boolean[] isFlag;
    private ImageAdapter imageAdapter;

    Minesweeper(final Context context, GridView container, Level level) {
        switch (level) {
            case EASY:
                row = 8;
                column = 8;
                break;
            case NORMAL:
                row = 16;
                column = 16;
                mineNumber = (int) (row * column * 15.625 / 100);
                break;
            case HARD:
                row = 16;
                column = 30;
                mineNumber = (int) (row * column * 20.625 / 100);
                break;
        }
        size = row * column;
        mineNumber = (int) (size * (level.equals(Level.HARD) ? 20.625 : 15.625) / 100);
        countDown = size - mineNumber;
        isMapCreated = false;
        isGameDone = false;
        boomPosition = -1;
        isOpen = new boolean[size];
        Arrays.fill(isOpen, false);
        isFlag = new boolean[size];
        Arrays.fill(isFlag, false);
        init(context, container);
    }

    void init(final Context context, GridView container) {
        int iconWidth;
        BitmapDrawable bitmapDrawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bitmapDrawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_ball, context.getTheme());
        } else {
            //noinspection deprecation
            bitmapDrawable = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_ball));
        }

        container.setNumColumns(column);
        if (bitmapDrawable != null) {
            iconWidth = bitmapDrawable.getBitmap().getWidth();
            container.setStretchMode(GridView.NO_STRETCH);
            container.setColumnWidth(iconWidth);
            container.getLayoutParams().width = iconWidth * column;
        } else {
            container.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        }

        imageAdapter = new ImageAdapter(context);
        container.setAdapter(imageAdapter);

        container.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                if (!isMapCreated) {
                    createMap(position);
                    isMapCreated = true;
                }
                if (!isGameDone && !isOpen[position] && !isFlag[position]) {
                    int value = getMapValue(position);
                    if (value < 0) {
                        boomPosition = position;
                        gameOver();

                        new AlertDialog.Builder(context)
                                .setMessage(context.getString(R.string.game_over))
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                    } else {
                        expand(position);

                        if (countDown == 0) {
                            isGameDone = true;
                            new AlertDialog.Builder(context)
                                    .setMessage(context.getString(R.string.game_clear))
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                        }
                    }
                }
            }
        });

        container.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (!isGameDone && !isOpen[position]) {
                    if (isFlag[position]) {
                        view.setBackgroundResource(R.drawable.ic_ball);
                        isFlag[position] = false;
                    } else {
                        view.setBackgroundResource(R.drawable.ic_flag);
                        isFlag[position] = true;
                    }
                }
                return true;
            }
        });
    }

    private void createMap(int position) {
        map = new int[row][column];
        for (int count = 0; count < mineNumber; ) {
            int pos = (int) (Math.random() * size);
            if (pos == position) {
                continue;
            }
            int m = pos / column;
            int n = pos % column;

            if (map[m][n] >= 0) {
                map[m][n] = -count - 1;
                count++;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (i != 0 || j != 0) {
                            if (m + i >= 0  && m + i < row && n + j >= 0 && n + j < column) {
                                if (map[m + i][n + j] >= 0) {
                                    map[m + i][n + j]++;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private int getMapValue(int position) {
        return map[position / column][position % column];
    }

    private void open(int position) {
        if (isFlag[position]) {
            return;
        }
        Button button = (Button) imageAdapter.getItem(position);
        setState(position, button);
        isOpen[position] = true;
    }

    private void setState(int position, Button button) {
        int value = getMapValue(position);
        if (value == 0) {
            button.setBackgroundResource(R.color.emptyGrid);
        } else {
            if (value > 0) {
                button.setBackgroundResource(R.color.numberGrid);
                button.setText(String.format(Locale.getDefault(), "%d", value));
            } else {
                button.setBackgroundResource(R.drawable.ic_mine);
            }
        }
    }

    private void gameOver() {
        isGameDone = true;
        isOpen[boomPosition] = true;
        for (int i = 0; i < size; i++) {
            View view = (View) imageAdapter.getItem(i);
            if (isFlag[i]) {
                if (getMapValue(i) >= 0) {
                    view.setBackgroundResource(R.drawable.ic_mine_error);
                }
            } else if (getMapValue(i) < 0) {
                if (i == boomPosition) {
                    view.setBackgroundResource(R.drawable.ic_mine_boom);
                } else {
                    open(i);
                }
            }
        }
    }

    private void expand(int position) {
        expand(position / column, position % column);
    }

    private void expand(int r, int c) {
        if (isOpen[r * column + c]) {
            return;
        }
        open(r * column + c);
        countDown--;
        if (map[r][c] != 0) {
            return;
        }
        boolean checkUp = r > 0;
        boolean checkRight = c + 1 < column;
        boolean checkDown = r + 1 < row;
        boolean checkLeft = c > 0;

        if (checkUp) {
            expand(r - 1, c);
        }
        if (checkUp && checkRight) {
            expand(r - 1, c + 1);
        }
        if (checkRight) {
            expand(r, c + 1);
        }
        if (checkRight && checkDown) {
            expand(r + 1, c + 1);
        }
        if (checkDown) {
            expand(r + 1, c);
        }
        if (checkDown && checkLeft) {
            expand(r + 1, c - 1);
        }
        if (checkLeft) {
            expand(r, c - 1);
        }
        if (checkLeft && checkUp) {
            expand(r - 1, c - 1);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(row);
        dest.writeInt(column);
        dest.writeInt(size);
        dest.writeInt(mineNumber);
        dest.writeInt(countDown);
        dest.writeInt(boomPosition);
        for (int[] aMap : map) {
            dest.writeIntArray(aMap);
        }
        dest.writeByte((byte) (isMapCreated ? 1 : 0));
        dest.writeByte((byte) (isGameDone ? 1 : 0));
        dest.writeBooleanArray(isOpen);
        dest.writeBooleanArray(isFlag);
    }

    private Minesweeper(Parcel in) {
        row = in.readInt();
        column = in.readInt();
        size = in.readInt();
        mineNumber = in.readInt();
        countDown = in.readInt();
        boomPosition = in.readInt();
        map = new int[row][column];
        for (int i = 0; i < row; i++) {
            map[i] = in.createIntArray();
        }
        isMapCreated = (in.readByte() == 1);
        isGameDone = (in.readByte() == 1);
        in.readBooleanArray(isOpen);
        in.readBooleanArray(isFlag);
    }

    public static final Parcelable.Creator<Minesweeper> CREATOR
            = new Parcelable.Creator<Minesweeper>() {

        @Override
        public Minesweeper createFromParcel(Parcel source) {
            return new Minesweeper(source);
        }

        @Override
        public Minesweeper[] newArray(int size) {
            return new Minesweeper[0];
        }
    };

    enum Level {EASY, NORMAL, HARD}

    private class ImageAdapter extends BaseAdapter {
        private List<Button> buttons = new ArrayList<>(size);

        ImageAdapter(Context context) {
            for (int i = 0; i < size; i++) {
                Button button = new Button(context);
                button.setFocusable(false);
                button.setClickable(false);
                button.setLayoutParams(new GridView.LayoutParams(
                        GridView.LayoutParams.WRAP_CONTENT,
                        GridView.LayoutParams.WRAP_CONTENT));
                if (isFlag[i]) {
                    if (isGameDone && getMapValue(i) >= 0) {
                        button.setBackgroundResource(R.drawable.ic_mine_error);
                    } else {
                        button.setBackgroundResource(R.drawable.ic_flag);
                    }
                } else if (isOpen[i]) {
                    if (i == boomPosition) {
                        button.setBackgroundResource(R.drawable.ic_mine_boom);
                    } else {
                        setState(i, button);
                    }
                } else {
                    button.setBackgroundResource(R.drawable.ic_ball);
                }
                buttons.add(i, button);
            }
        }

        @Override
        public int getCount() {
            return size;
        }

        @Override
        public Object getItem(int position) {
            return buttons.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return buttons.get(position);
        }
    }
}
