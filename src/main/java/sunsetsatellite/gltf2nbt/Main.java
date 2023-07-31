package sunsetsatellite.gltf2nbt;

import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.*;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import dev.dewy.nbt.Nbt;
import dev.dewy.nbt.io.CompressionType;
import dev.dewy.nbt.tags.collection.CompoundTag;
import dev.dewy.nbt.tags.primitive.IntTag;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        if(args.length != 2){
            throw new Error("Expected 2 arguments, got "+args.length+".");
        }
        convertFile(args[0],args[1]);
    }

    public static void convertFile(String path, String savePath) {
        System.out.println("Loading model: "+path);
        try {
            File file = new File(path);
            GltfModelReader reader = new GltfModelReader();
            GltfModel model = reader.read(file.toURI());
            System.out.printf("Model loaded from: %s%n",path);
            List<NodeModel> nodes = model.getNodeModels();
            System.out.printf("%s nodes%n",nodes.size());
            List<MeshModel> meshes = model.getMeshModels();
            List<ImageModel> textures = model.getImageModels();
            System.out.printf("%s textures%n",textures.size());
            List<MaterialModel> materials = model.getMaterialModels();
            System.out.printf("%s materials%n",materials.size());
            HashMap<String,Integer> texRemap = new HashMap<>();
            int k = 0;
            for (MaterialModel materialBase : materials) {
                MaterialModelV2 material = (MaterialModelV2) materialBase;
                texRemap.put(material.getBaseColorTexture().getName(),k);
                k++;
            }
            System.out.printf("%s meshes%n",meshes.size());
            Surface[] surfaces = new Surface[meshes.size()];
            Nbt nbt = new Nbt();
            CompoundTag root = new CompoundTag();
            int i = 0;
            for (NodeModel node : nodes) {
                List<MeshModel> nodeMeshes = node.getMeshModels();
                for (MeshModel nodeMesh : nodeMeshes) {
                    for (MeshPrimitiveModel primitiveModel : nodeMesh.getMeshPrimitiveModels()) {
                        Vec3f translation;
                        Vec4f rotation;
                        Vec3f scale;
                        if(node.getTranslation() != null){
                            translation = new Vec3f(node.getTranslation()[0],node.getTranslation()[1],node.getTranslation()[2]);
                        } else {
                            translation = new Vec3f();
                        }
                        if(node.getRotation() != null){
                            rotation = new Vec4f(node.getRotation()[0],node.getRotation()[1],node.getRotation()[2],node.getRotation()[3]);
                        } else {
                            rotation = new Vec4f(0,0,0,1);
                        }
                        if(node.getScale() != null){
                            scale = new Vec3f(node.getScale()[0],node.getScale()[1],node.getScale()[2]);
                        } else {
                            scale = new Vec3f(1);
                        }
                        MaterialModelV2 material = (MaterialModelV2) primitiveModel.getMaterialModel();
                        AccessorModel indexAttribute = primitiveModel.getIndices();
                        int[] indices = new int[]{6,4,5,7,3,1,4,6,5,0,2,7,2,0,1,3,1,0,5,4,6,7,2,3};
                        Map<String, AccessorModel> attributes = primitiveModel.getAttributes();
                        AccessorModel positionAttribute = attributes.get("POSITION");
                        AccessorModel uvAtrribute = attributes.get("TEXCOORD_0");
                        AccessorModel normalAttribute = attributes.get("NORMAL");
                        Vec3f[] vertices = dataToVec3fArray((AccessorFloatData) positionAttribute.getAccessorData());
                        Vec2f[] uvs = dataToVec2fArray((AccessorFloatData) uvAtrribute.getAccessorData());
                        Vec3f[] normals = dataToVec3fArray((AccessorFloatData) normalAttribute.getAccessorData());
                        int texture = texRemap.get(material.getBaseColorTexture().getName());
                        surfaces[i] = new Surface(vertices,uvs,indices,normals,texture,translation,rotation,scale);
                        i++;
                    }
                }
            }
            i = 0;
            for (Surface surface : surfaces) {
                CompoundTag surfaceTag = new CompoundTag("Surface "+i);
                CompoundTag indicesTag = new CompoundTag("Indices");
                CompoundTag verticesTag = new CompoundTag("Vertices");
                int j = 0;
                for (int index : surface.indices) {
                    indicesTag.putInt(String.valueOf(j),index);
                    j++;
                }
                j = 0;
                for (Vec3f vertex : surface.vertices) {
                    CompoundTag vertexTag = new CompoundTag(String.valueOf(j));
                    vertexTag.put(vertex.writeToNBT(new CompoundTag("XYZ")));
                    vertexTag.put(surface.uvs[j].writeToNBT(new CompoundTag("UV")));
                    vertexTag.put(surface.normals[j].writeToNBT(new CompoundTag("Normal")));
                    verticesTag.put(vertexTag);
                    j++;
                }
                surfaceTag.put(surface.translation.writeToNBT(new CompoundTag("Translation")));
                surfaceTag.put(surface.rotation.writeToNBT(new CompoundTag("Rotation")));
                surfaceTag.put(surface.scale.writeToNBT(new CompoundTag("Scale")));
                surfaceTag.put(new IntTag("Texture",surface.texture));
                surfaceTag.put(indicesTag);
                surfaceTag.put(verticesTag);
                root.put(surfaceTag);
                i++;
            }
            File saveFile = new File(savePath);
            nbt.toFile(root,saveFile, CompressionType.GZIP);
            System.out.println("Saved to: "+savePath);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Vec3f[] dataToVec3fArray(AccessorFloatData data){
        Vec3f[] vec3f = new Vec3f[data.getNumElements()];
        for (int i = 0; i < data.getNumElements(); i++) {
            float[] components = new float[data.getNumComponentsPerElement()];
            for (int j = 0; j < data.getNumComponentsPerElement(); j++) {
                components[j] = data.get(i,j);
            }
            vec3f[i] = new Vec3f(components[0],components[1],components[2]);
        }
        return vec3f;
    }

    public static Vec2f[] dataToVec2fArray(AccessorFloatData data){
        Vec2f[] vec2f = new Vec2f[data.getNumElements()];
        for (int i = 0; i < data.getNumElements(); i++) {
            float[] components = new float[data.getNumComponentsPerElement()];
            for (int j = 0; j < data.getNumComponentsPerElement(); j++) {
                components[j] = data.get(i,j);
            }
            vec2f[i] = new Vec2f(components[0],components[1]);
        }
        return vec2f;
    }
}