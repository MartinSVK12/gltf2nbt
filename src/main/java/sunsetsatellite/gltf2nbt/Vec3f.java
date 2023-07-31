package sunsetsatellite.gltf2nbt;

import dev.dewy.nbt.tags.collection.CompoundTag;

public class Vec3f {
    public double x;
    public double y;
    public double z;

    public Vec3f(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    };

    public Vec3f(){
        this.x = this.y = this.z = 0;
    }

    public Vec3f(double size){
        this.x = this.y = this.z = size;
    }


    public double distanceTo(Vec3f vec3f) {
        double d = vec3f.x - this.x;
        double d1 = vec3f.y - this.y;
        double d2 = vec3f.z - this.z;
        return Math.sqrt(d * d + d1 * d1 + d2 * d2);
    }

    public Vec3f add(double value){
        this.x += value;
        this.y += value;
        this.z += value;
        return this;
    }

    public Vec3f subtract(double value){
        this.x -= value;
        this.y -= value;
        this.z -= value;
        return this;
    }

    public Vec3f divide(double value){
        this.x /= value;
        this.y /= value;
        this.z /= value;
        return this;
    }

    public Vec3f multiply(double value){
        this.x *= value;
        this.y *= value;
        this.z *= value;
        return this;
    }

    public Vec3f add(Vec3f value){
        this.x += value.x;
        this.y += value.y;
        this.z += value.z;
        return this;
    }

    public Vec3f subtract(Vec3f value){
        this.x -= value.x;
        this.y -= value.y;
        this.z -= value.z;
        return this;
    }

    public Vec3f divide(Vec3f value){
        this.x /= value.x;
        this.y /= value.y;
        this.z /= value.z;
        return this;
    }

    public Vec3f multiply(Vec3f value){
        this.x *= value.x;
        this.y *= value.y;
        this.z *= value.z;
        return this;
    }

    public CompoundTag writeToNBT(CompoundTag tag){
        tag.putDouble("x",this.x);
        tag.putDouble("y",this.y);
        tag.putDouble("z",this.z);
        return tag;
    }

    public void readFromNBT(CompoundTag tag){
        this.x = tag.getDouble("x").getValue();
        this.y = tag.getDouble("y").getValue();
        this.z = tag.getDouble("z").getValue();
    }

    public Vec3f copy(){
        return new Vec3f(this.x,this.y,this.z);
    }


    @Override
    public String toString() {
        return "("+x+","+y+","+z+")";
        /*return "Vec3{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';*/
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vec3f vec3I = (Vec3f) o;

        if (x != vec3I.x) return false;
        if (y != vec3I.y) return false;
        return z == vec3I.z;
    }

    @Override
    public int hashCode() {
        int result = (int) x;
        result = (int) (31 * result + y);
        result = (int) (31 * result + z);
        return result;
    }
}
