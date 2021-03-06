//Yakomovich Alexander 8O-308B
package lab4.Misc;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL20.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;

public class Shader {
    private int prog;
    private int vertShader;
    private int fragShader;

    public Shader(String fileName) {
        prog = glCreateProgram();

        vertShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertShader, readFile(fileName + ".vs"));
        glCompileShader(vertShader);
        if (glGetShaderi(vertShader, GL_COMPILE_STATUS) != 1) {
            System.err.println(glGetShaderInfoLog(vertShader));
            System.exit(1);
        }

        fragShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragShader, readFile(fileName + ".fs"));
        glCompileShader(fragShader);
        if (glGetShaderi(fragShader, GL_COMPILE_STATUS) != 1) {
            System.err.println(glGetShaderInfoLog(fragShader));
            System.exit(1);
        }
        glAttachShader(prog, vertShader);
        glAttachShader(prog, fragShader);

        glBindAttribLocation(prog, 0, "vert");

        glLinkProgram(prog);
        if (glGetProgrami(prog, GL_LINK_STATUS) != 1) {
            System.err.println(glGetProgramInfoLog(prog));
            System.exit(1);
        }
        glValidateProgram(prog);
        if (glGetProgrami(prog, GL_VALIDATE_STATUS) != 1) {
            System.err.println(glGetProgramInfoLog(prog));
            System.exit(1);
        }
    }


    public void setUniform(String name,Matrix4f value){
        int location = glGetUniformLocation(prog,name);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        value.get(buffer);
        if(location != -1){
            glUniformMatrix4fv(location,false,buffer);
        }
    }

    public void setUniform(String name,Vector4f value){
        int location = glGetUniformLocation(prog,name);
        if(location != -1){
            glUniform3f(location,value.x,value.y,value.z);
        }
    }

    public void bind() {
        glUseProgram(prog);
    }

    private String readFile(String fileName) {
        StringBuilder string = new StringBuilder();
        BufferedReader reader;
        try {
            reader = new BufferedReader(
                    new FileReader(
                            new File(getClass().getResource("/shaders/"+fileName).toString().substring(5))));
            String line;
            while ((line = reader.readLine()) != null) {
                string.append(line);
                string.append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return string.toString();
    }
}
