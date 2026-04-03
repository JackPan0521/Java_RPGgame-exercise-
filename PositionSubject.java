interface PositionSubject {
    void addObserver(PositionObserver o);
    void removeObserver(PositionObserver o);
    void onPositionChanged(int x,int y);
}
