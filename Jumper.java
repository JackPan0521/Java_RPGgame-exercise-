public class Jumper implements IMoveState {
    public void move(SampleRole5 r) {

        if (r.getJumpAbility()) {

            // 垂直移動
            r.setY(r.getY() + r.getJVY());

            // 重力
            r.setJVY(r.getJVY() + 10);

            // 落地檢查
            if (r.getY() >= r.getBottom()) {

                r.setY(r.getBottom());
                r.setJumpAbility(false);

                // 重置速度
                r.setJVY(0);

                // 回到站立
                r.setDim1(0);
                r.setDim2(0);
            }
        }

        // 水平移動
        r.setX(r.getX() + r.getDX());
    }
}