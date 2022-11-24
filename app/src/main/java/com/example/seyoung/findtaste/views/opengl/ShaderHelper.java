package com.example.seyoung.findtaste.views.opengl;

import android.opengl.GLES20;
import android.util.Log;

/**
 * Created by seyoung on 2018-01-11.
 * 사용자가 도형을 그릴 때 도형을 만들어 줄 수 있도록 해주는 쉐이더컴파일
 */

public class ShaderHelper {
    private static final String TAG = "ShaderHelper";

    /**
     * 셰이더를 컴파일하는 도우미 함수.
     *
     * @param shaderType 쉐이더 타입입니다.
     * @param shaderSource 셰이더 소스 코드.
     * @return 셰이더에 대한 OpenGL 핸들입니다.
     */
    public static int compileShader(final int shaderType, final String shaderSource)
    {
        int shaderHandle = GLES20.glCreateShader(shaderType);

        if (shaderHandle != 0)
        {
            // 셰이더 소스를 전달하십시오.
            GLES20.glShaderSource(shaderHandle, shaderSource);

            // 셰이더를 컴파일하십시오.
            GLES20.glCompileShader(shaderHandle);

            // 컴파일 상태를 얻습니다.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // 컴파일이 실패한 경우 셰이더를 삭제하십시오.
            if (compileStatus[0] == 0)
            {
                Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandle));
                GLES20.glDeleteShader(shaderHandle);
                shaderHandle = 0;
            }
        }

        if (shaderHandle == 0)
        {
            throw new RuntimeException("Error creating shader.");
        }

        return shaderHandle;
    }

    /**
     * 프로그램을 컴파일하고 링크하는 도우미 기능.
     *
     * @param vertexShaderHandle 이미 컴파일 된 버텍스 쉐이더에 대한 OpenGL 핸들.
     * @param fragmentShaderHandle 벌써 컴파일 된 fragment shader에 대한 OpenGL 핸들.
     * @param attributes 프로그램에 묶일 필요가있는 ttributes.
     * @return 프로그램에 대한 OpenGL 핸들입니다.
     */
    public static int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle, final String[] attributes)
    {
        int programHandle = GLES20.glCreateProgram();

        if (programHandle != 0)
        {
            // 버텍스 쉐이더를 프로그램에 바인딩하십시오.
            GLES20.glAttachShader(programHandle, vertexShaderHandle);

            // 조각 쉐이더를 프로그램에 바인드합니다.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            // 컴파일이 실패한 경우 셰이더를 삭제하십시오.
            if (attributes != null)
            {
                final int size = attributes.length;
                for (int i = 0; i < size; i++)
                {
                    GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
                }
            }

            // 두 개의 셰이더를 함께 프로그램에 연결하십시오.
            GLES20.glLinkProgram(programHandle);

            // 링크 상태를 가져옵니다.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // 링크가 실패하면 프로그램을 삭제하십시오.
            if (linkStatus[0] == 0)
            {
                Log.e(TAG, "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle));
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0)
        {
            throw new RuntimeException("Error creating program.");
        }

        return programHandle;
    }
}