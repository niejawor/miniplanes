package model;

/*

 */

public final class Point {
    private float x, y;

    public Point() { x = y = 0; }

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point point) {
        x = point.getX();
        y = point.getY();
    }

    public float distance(Point p){
        return (float)Math.sqrt((p.x - x)*(p.x - x) + (p.y - y)*(p.y - y));
    }

    public float distance(float x, float y) {
        return (float)Math.sqrt((this.x - x)*(this.x - x) + (this.y - y)*(this.y - y));
    }

    public void move(float a, float b) { x+=a; y+=b; }

    public void set(float x, float y){
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Point getCopy(){
        return new Point(x,y);
    }
}
