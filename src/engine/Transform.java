package engine;
import math.Mat4;
import math.Vec3;
public final class Transform {
    private Vec3 position = new Vec3(0, 0, 0);
    private Vec3 rotation = new Vec3(0,0,0);
    private Vec3 scale = new Vec3(1,1,1);

    public Vec3 getPosition(){return position;}
    public Vec3 getRotation(){return rotation;}
    public Vec3 getScale() {return scale;}

    public void setPosition(Vec3 p){position = p;}
    public void setRotation(Vec3 r){rotation = r;}

    public void setScale(Vec3 s) {scale = s;}

    public Mat4 toMatrix() {
        Mat4 t  = Mat4.translate(position.x, position.y, position.z);
        Mat4 ry = Mat4.rotateY(rotation.y);
        Mat4 rx = Mat4.rotateX(rotation.x);
        Mat4 rz = Mat4.rotateZ(rotation.z);
        Mat4 s  = Mat4.scale(scale.x, scale.y, scale.z);
        return t.multiply(rz).multiply(ry).multiply(rx).multiply(s);
    }
}
