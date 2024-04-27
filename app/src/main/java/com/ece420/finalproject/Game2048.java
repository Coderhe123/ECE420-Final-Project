package com.ece420.finalproject;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Game2048 {
    final private int ROWS = 4;
    final private int COLS = 4;
    private int [][] board;

    public Game2048() {
        board = new int [4][4];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++){
                board[i][j] = 0;
            }
        }
        Create(2);
    }

    public Game2048(int [][] arr) {
        board = new int [4][4];
        if (arr.length != ROWS || arr[0].length != COLS) {
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++){
                    board[i][j] = 0;
                }
            }
            Create(2);
        }
        else {
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++){
                    board[i][j] = arr[i][j];
                }
            }
        }
    }

    public void copy_(int [][] arr) {
        if (arr.length != ROWS || arr[0].length != COLS) return;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++){
                board[i][j] = arr[i][j];
            }
        }
    }

    private void copy_(Game2048 game){
        int [][] arr = game.getBoard();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++){
                board[i][j] = arr[i][j];
            }
        }
    }
    private void Create(){
        // Default to create 1 new block
        Create(1);
    }
    private boolean Create(int num){
        // Get the available coordinates
        List<Pair<Integer, Integer>> coords = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++){
                if (board[i][j] == 0){
                    coords.add(new Pair<>(i, j));
                }
            }
        }

        // Random shuffle to get a random order
        Collections.shuffle(coords);
        int[] vals = new int[]{2, 2, 2, 2, 2, 2, 2, 2, 2, 4};
        Random random = new Random();
        int cnt = 0;
        for (Pair<Integer, Integer> coord: coords) {
            int row = coord.first, col = coord.second;
            // Set those blank coordinates with random new value, 90% 2 and 10% 2
            board[row][col] = vals[random.nextInt(vals.length)];
            cnt++;
            if (cnt == num) return true;
        }
        return false;
    }

    private boolean MovRowLeft(int row) {
        int [] orig_row = new int[COLS];
        for (int j = 0; j < COLS; j++) orig_row[j] = board[row][j];

        int [] shifted_row = new int[COLS];
        int idx = 0;
        for (int j = 0; j < COLS; j++) {
            if (board[row][j] != 0) {
                shifted_row[idx++] = board[row][j];
            }
        }
        for (int j = 0; j < idx - 1; j++) {
            if (shifted_row[j] == shifted_row[j + 1]) {
                shifted_row[j] *= 2;
                shifted_row[j + 1] = 0;
            }
        }
        idx = 0;
        for (int j = 0; j < COLS; j++) {
            if (shifted_row[j] != 0) {
                board[row][idx++] = shifted_row[j];
            }
        }
        for (int j = idx; j < COLS; j++) board[row][j] = 0;
        for (int j = 0; j < COLS; j++) {
            if (orig_row[j] != board[row][j]){
                return true;
            }
        }
        return false;
    }

    private boolean MovRowRight(int row) {
        int [] orig_row = new int[COLS];
        for (int j = 0; j < COLS; j++) orig_row[j] = board[row][j];

        int [] shifted_row = new int[COLS];
        int idx = COLS - 1;
        for (int j = COLS - 1; j >= 0; j--) {
            if (board[row][j] != 0) {
                shifted_row[idx--] = board[row][j];
            }
        }
        for (int j = COLS - 1; j >= idx + 2; j--) {
            if (shifted_row[j] == shifted_row[j - 1]) {
                shifted_row[j] *= 2;
                shifted_row[j - 1] = 0;
            }
        }
        idx = COLS - 1;
        for (int j = COLS - 1; j >= 0; j--) {
            if (shifted_row[j] != 0) {
                board[row][idx--] = shifted_row[j];
            }
        }
        for (int j = 0; j <= idx; j++) board[row][j] = 0;
        for (int j = 0; j < COLS; j++) {
            if (orig_row[j] != board[row][j]){
                return true;
            }
        }
        return false;
    }

    private boolean MovColUp(int col) {
        int [] orig_col = new int[ROWS];
        for (int i = 0; i < ROWS; i++) orig_col[i] = board[i][col];

        int [] shifted_col = new int[ROWS];
        int idx = 0;
        for (int i = 0; i < ROWS; i++) {
            if (board[i][col] != 0) {
                shifted_col[idx++] = board[i][col];
            }
        }
        for (int i = 0; i < idx - 1; i++) {
            if (shifted_col[i] == shifted_col[i + 1]) {
                shifted_col[i] *= 2;
                shifted_col[i + 1] = 0;
            }
        }
        idx = 0;
        for (int i = 0; i < ROWS; i++) {
            if (shifted_col[i] != 0) {
                board[idx++][col] = shifted_col[i];
            }
        }
        for (int i = idx; i < ROWS; i++) board[i][col] = 0;
        for (int i = 0; i < ROWS; i++) {
            if (orig_col[i] != board[i][col]){
                return true;
            }
        }
        return false;
    }

    private boolean MovColDown(int col) {
        int [] orig_col = new int[ROWS];
        for (int i = 0; i < ROWS; i++) orig_col[i] = board[i][col];

        int [] shifted_col = new int[ROWS];
        int idx = ROWS - 1;
        for (int i = ROWS - 1; i >= 0; i--) {
            if (board[i][col] != 0) {
                shifted_col[idx--] = board[i][col];
            }
        }
        for (int i = ROWS - 1; i >= idx + 2; i--) {
            if (shifted_col[i] == shifted_col[i - 1]) {
                shifted_col[i] *= 2;
                shifted_col[i - 1] = 0;
            }
        }
        idx = ROWS - 1;
        for (int i = ROWS - 1; i >= 0; i--) {
            if (shifted_col[i] != 0) {
                board[idx--][col] = shifted_col[i];
            }
        }
        for (int i = 0; i <= idx; i++) board[i][col] = 0;
        for (int i = 0; i < ROWS; i++) {
            if (orig_col[i] != board[i][col]){
                return true;
            }
        }
        return false;
    }

    public boolean MovLeft() {
        boolean flag = false;
        for (int i = 0; i < ROWS; i++)
            flag |= MovRowLeft(i);
        return flag;
    }

    public boolean MovRight() {
        boolean flag = false;
        for (int i = 0; i < ROWS; i++)
            flag |= MovRowRight(i);
        return flag;
    }

    public boolean MovUp() {
        boolean flag = false;
        for (int j = 0; j < COLS; j++)
            flag |= MovColUp(j);
        return flag;
    }

    public boolean MovDown() {
        boolean flag = false;
        for (int j = 0; j < COLS; j++)
            flag |= MovColDown(j);
        return flag;
    }

    public boolean Command(int cmd) {
        if (cmd == 0) return MovLeft();
        else if (cmd == 1) return MovRight();
        else if (cmd == 2) return MovUp();
        else if (cmd == 3) return MovDown();
        return false;
    }

    public boolean GameOver() {
        for (int i = 0; i < ROWS; i++)
            for (int j = 0; j < COLS; j++)
                if (board[i][j] == 0)
                    return false;

        for (int i = 0; i < ROWS; i++)
            for (int j = 0; j < COLS - 1; j++)
                if (board[i][j] == board[i][j + 1])
                    return false;

        for (int j = 0; j < COLS; j++)
            for (int i = 0; i < ROWS - 1; i++)
                if (board[i][j] == board[i + 1][j])
                    return false;

        return true;
    }
    public int[][] getBoard() {
        return board;
    }

    public void printBoard() {
        Log.d("debug matrix", "------new debug starts------");
        for (int i = 0; i < ROWS; i++) {
            String s = "";
            for (int j = 0; j < COLS; j++) {
                s += String.valueOf(board[i][j]) + "   ";
            }
            Log.d("debug matrix", s);
        }
    }

    public double Score() {
        double s = 0;
        for (int i = 0; i < ROWS; i++){
            for (int j = 0; j < COLS; j++){
                int d1 = i + j, d2 = i + COLS - 1 - j, v = 0;
                if (d1 == 0 || d2 == 0) v = 0;
                else v = d1 + d2 - 1;
                s += Math.pow(board[i][j], 2) / Math.pow(4, v);
            }
        }
        return s;
    }

    public double Search(int depth, boolean move) {
        if (depth == 0) return Score();
        if (move == true && GameOver()) return -1e6;

        if (move == true) {
            double max_val = 0.0;
            Game2048 CopyBoard = new Game2048(board);
            boolean acted = false;
            for (int i = 0; i < 3; i++) {
                boolean flag = Command(i);
                if (flag == false) continue;
                acted = true;
                max_val = Math.max(max_val, Search(depth - 1, !move));
                copy_(CopyBoard);
            }
            if (acted == false) {
                boolean flag = Command(3);
                if (flag == true) {
                    max_val = Math.max(max_val, Search(depth - 1, !move));
                    copy_(CopyBoard);
                }
            }
            return max_val;
        }

        else {
            double sum_val = 0.0;
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    if (board[i][j] == 0) {
                        double coeff = (4 - i) * (4 - i) / 64.0;
                        board[i][j] = 2;
                        sum_val += 0.9 * coeff * Search(depth - 1, !move);
                        board[i][j] = 4;
                        sum_val += 0.1 * coeff * Search(depth - 1, !move);
                        board[i][j] = 0;
                    }
                }
            }
            return sum_val;
        }
    }

    public int AImove() {
        Game2048 CopyBoard = new Game2048(board);
        double max_val = -1e8;
        int action = -1;

        for (int i = 0; i < 3; i++) {
            boolean flag = Command(i);
            if (flag == false) continue;
            double val_i = Search(3, false);
            copy_(CopyBoard);
            if (max_val < val_i) {
                max_val = val_i;
                action = i;
            }
        }

        if (action == -1) {
            boolean flag = Command(3);
            if (flag == true) {
                double val_i = Search(3, false);
                copy_(CopyBoard);
                if (max_val < val_i) {
                    max_val = val_i;
                    action = 3;
                }
            }
        }

        return action;
    }

    public void playWithAI() {
        while (true) {
            if (GameOver()) {
                int max_val = 0;
                for (int i = 0; i < ROWS; i++) {
                    for (int j = 0; j < COLS; j++) {
                        max_val = Math.max(max_val, board[i][j]);
                    }
                }
                printBoard();
                Log.d("EndScore = ", String.valueOf(max_val));
                break;
            }
            int action = AImove();
            if (0 <= action && action <= 3) Command(action);
            else {
                printBoard();
                Log.d("debug matrix ", "Action = " + action + " No action for moving");
                break;
            }
            Create();
        }
    }
}
