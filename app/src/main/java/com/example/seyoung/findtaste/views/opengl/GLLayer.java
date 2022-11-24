package com.example.seyoung.findtaste.views.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.seyoung.findtaste.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by seyoung on 2018-01-11.
 */

public class GLLayer implements GLSurfaceView.Renderer {
    /**
     *
     이 클래스는 커스텀 렌더러를 구현합니다. GL10 매개 변수
     전달 된 OpenGL ES 2.0 렌더러는 정적 클래스입니다.
     대신 GLES20이 사용됩니다.
     */
    private final Context mActivityContext;

    /**
     * 모델 행렬을 저장하십시오.
     이 행렬은 모델 공간 (각 모델이 우주의 중심에 위치한다고 생각할 수있는)에서
     세계 공간으로 모델을 이동하는 데 사용됩니다.
     */
    private float[] mModelMatrix = new float[16];

    /**
     * 뷰 매트릭스를 저장하십시오. 이것은 우리 카메라로 생각할 수 있습니다.
     이 행렬은 세계 공간을 시각 공간으로 변환합니다.
     그것은 우리의 눈에 상대적으로 물건을 배치합니다.
     */
    private float[] mViewMatrix = new float[16];

    /**
     투영 행렬을 저장하십시오. 장면을 2D 뷰포트에 투영하는 데 사용됩니다.
     */
    private float[] mProjectionMatrix = new float[16];

    /**
     최종 결합 된 매트릭스에 대한 스토리지를 할당하십시오.
     이것은 셰이더 프로그램에 전달됩니다.
     */
    private float[] mMVPMatrix = new float[16];

    /** 모델 데이터를 플로트 버퍼에 저장하십시오.*/
    private final FloatBuffer mCubePositions;
    private final FloatBuffer mCubeColors;
    private final FloatBuffer mCubeTextureCoordinates;

    /** 이것은 변환 행렬을 전달하는 데 사용됩니다. */
    private int mMVPMatrixHandle;

    /** 이것은 텍스처를 전달하는 데 사용됩니다.*/
    private int mTextureUniformHandle0;
    private int mTextureUniformHandle1;

    /** 이것은 모델 위치 정보를 전달하는 데 사용됩니다. */
    private int mPositionHandle;

    /** 이것은 모델 색상 정보를 전달하는 데 사용됩니다.*/
    // private int mColorHandle;

    /** 이것은 모델 텍스처 좌표 정보를 전달하는 데 사용됩니다.*/
    private int mTextureCoordinateHandle;

    /** float 당 몇 바이트입니까?*/
    private final int mBytesPerFloat = 4;

    /** 요소의 위치 데이터 크기입니다.*/
    private final int mPositionDataSize = 3;

    /** 요소의 색상 데이터 크기입니다.*/
    // private final int mColorDataSize = 4;

    /** 요소 내의 텍스처 좌표 데이터의 크기.*/
    private final int mTextureCoordinateDataSize = 2;

    /** 이것은 큐브 쉐이딩 프로그램의 핸들입니다. */
    private int mProgramHandle;

    /**
     이것은 텍스처 데이터의 핸들입니다. */
    private int mTextureDataHandle0;
    private int mTextureDataHandle1;

    /**
     셰이더 제목
     */
    static public int shader_selection = 0;
    static public final int BLUR = 1;
    static public final int EDGE = 2;
    static public final int EMBOSS = 3;
    static public final int FILTER = 4;
    static public final int FLIP = 5;
    static public final int HUE = 6;
    static public final int LUM = 7;
    static public final int NEG = 8;
    static public final int TOON = 9;
    static public final int TWIRL = 10;
    static public final int WARP = 11;
    private String 사진경로 = null;
    //and more ...
    int width_surface , height_surface ;
    static public Bitmap returebitmap;
    /**
     * 모델 데이터를 초기화하십시오.
     */
    public GLLayer(final Context activityContext, final String image, final int filter) {
        mActivityContext = activityContext;
        사진경로 = image;
        shader_selection = filter;
        // 큐브에 대한 점을 정의하십시오.

        // X, Y, Z
        final float[] cubePositionData = {
                // OpenGL에서는 반 시계 방향으로 감겨 있습니다. 이것은 우리가 삼각형을 볼 때,
                // 포인트가 반 시계 방향이라면 우리는 "앞"을보고 있습니다.
                // 그렇지 않다면 우리는 뒤를보고 있습니다.
                // OpenGL은 일반적으로 객체의 뒷면을 나타내며 어쨌든 볼 수 없기 때문에
                // 모든 뒤 향하는 삼각형을 선택하는 최적화 기능을 제공합니다.

                // Front face
                -1.1f, 1.1f, 1.1f, -1.1f, -1.1f, 1.1f, 1.1f, 1.1f, 1.1f, -1.1f,
                -1.1f, 1.1f, 1.1f, -1.1f, 1.1f, 1.1f, 1.1f, 1.1f };

        // R, G, B, A
        final float[] cubeColorData = {
                // Front face (red)
                1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f };

        // X, Y, Z
        // 법선은 밝은 계산에 사용되며 표면의 평면에 직각을 가리키는 벡터입니다.
        // 큐브 모델의 경우 법선은 각면의 점에 직각이어야합니다.

        // S, T (or X, Y)
        // 텍스처 좌표 데이터입니다.
        // OpenGL은 Y 축이 위를 가리키는 동안 이미지가 Y 축을 아래쪽으로 향하게하기 때문에
        // (이미지가 아래로 내려감에 따라 값이 증가합니다),
        // Y 축을 뒤집어서 여기에서 조정합니다.
        // 더 중요한 것은 텍스처 좌표가 모든면에서 동일하다는 것입니다.

        final float[] cubeTextureCoordinateData = {
                // Front face
                0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 0.0f };

        // 버퍼를 초기화하십시오.
        mCubePositions = ByteBuffer
                .allocateDirect(cubePositionData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubePositions.put(cubePositionData).position(0);

        mCubeColors = ByteBuffer
                .allocateDirect(cubeColorData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeColors.put(cubeColorData).position(0);

        mCubeTextureCoordinates = ByteBuffer
                .allocateDirect(
                        cubeTextureCoordinateData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeTextureCoordinates.put(cubeTextureCoordinateData).position(0);
    }

    protected String getVertexShader() {
        return RawResourceReader.readTextFileFromRawResource(mActivityContext,
                R.raw._vertex_shader);
    }

    protected String getFragmentShader() {
        int id;
        switch (shader_selection){
            case BLUR: id = R.raw.blurring_fragment_shader; break;
            // 가우시안 필터처럼 약간 흐려진다
            case EDGE: id = R.raw.edge_detect_fragment_shader;break;
            //Canny처럼 검정색 바탕에 윤곽선이 흰색으로 그려진다
            case EMBOSS: id = R.raw.emboss_fragment_shader;break;
            //가장자리 감지 루미넌스 이미지를 적용하고 가장자리의 각도에 따라 이미지를 다르게 강조하여 엠보싱 이미지를 얻습니다.
            case FILTER: id = R.raw.filter_fragment_shader;break;
            //여러 사진을 겹치는 필터. 필요는 없을듯
            case FLIP: id = R.raw.flip_fragment_shader;break;
            // 위아래 반전 필터
            case HUE: id = R.raw.hueshift_fragment_shader;break;
            //흰색이 초록색으로, 빨간색도 초록색으로 된다.
            case LUM: id = R.raw.luminance_fragment_shader;break;
            //회색필터. 검정색에 좀 가깝다
            case NEG: id = R.raw.negative_fragment_shader;break;
            //살착 채도를 어둡게 만들어주는 필터. 분위기 잡을 때 좋을듯
            case TOON: id = R.raw.toon_fragment_shader;break;
            //만화처럼 물감느낌을 준다
            case TWIRL: id = R.raw.twirl_fragment_shader;break;
            //가운데가 일렁이는 느낌을 준다 //안쓸거
            case WARP: id = R.raw.warp_fragment_shader;break;
            //가운데로 압축시킨 느낌.      //안쓸거
            default: id = R.raw._fragment_shader;break;
        }

        return RawResourceReader.readTextFileFromRawResource(mActivityContext, id);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        // 배경 투명 색상을 검정색으로 설정하십시오.
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // 컬링을 사용하여 뒷면을 제거하십시오.
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // 아래의 glEnable () 호출은 OpenGL ES 1에서의 오버 헤드이며
        // OpenGL ES 2에서는 필요하지 않습니다.
        // 텍스처 매핑 사용
        // GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        // 원점 앞쪽에 눈을 위치시킵니다.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = -0.5f;

        // 우리는 거리를 바라보고있다.
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // 우리의 위로 벡터를 설정하십시오. 이것은 우리의 머리가 우리가 카메라를 들고 가리키는 곳입니다.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // 뷰 매트릭스를 설정합니다. 이 행렬은 카메라 위치를 나타내는 것이라고 할 수 있습니다.
        // 참고 : OpenGL 1에서는 ModelView 행렬이 사용됩니다.이 행렬은 모델과 뷰 행렬의 조합입니다.
        // OpenGL 2에서, 우리가 선택한다면 우리는 이 행렬을 따로 추적 할 수 있습니다.
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY,
                lookZ, upX, upY, upZ);


        // Load the texture
        mTextureDataHandle0 = TextureHelper.loadTexture(mActivityContext,
                사진경로);

        // Load the texture
        mTextureDataHandle1 = TextureHelper.loadTexture(mActivityContext,
                사진경로);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // OpenGL 뷰포트를 서페이스와 동일한 크기로 설정합니다.
        GLES20.glViewport(0, 0, width, height);

        // 새로운 투시 투영 행렬을 만듭니다. 너비는 종횡비에 따라 다르지만 높이는 동일하게 유지됩니다.
        final float ratio = (float) width / height;
        final float left = -1.0f;
        final float right = 1.0f;
        final float bottom = -0.7f;
        final float top =0.7f;
        final float near = 1.0f;
        final float far = 4.0f;
        width_surface = width;
        height_surface = height;
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        final String vertexShader = getVertexShader();
        final String fragmentShader = getFragmentShader();

        final int vertexShaderHandle = ShaderHelper.compileShader(
                GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = ShaderHelper.compileShader(
                GLES20.GL_FRAGMENT_SHADER, fragmentShader);

        mProgramHandle = ShaderHelper.createAndLinkProgram(vertexShaderHandle,
                fragmentShaderHandle, new String[] { "a_Position",
                        "a_TexCoordinate" });

        // 우리의 버텍스 당 조명 프로그램을 설정하십시오.
        GLES20.glUseProgram(mProgramHandle);

        // 큐브 그리기를위한 프로그램 핸들을 설정하십시오.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle,
                "u_MVPMatrix");
        mTextureUniformHandle0 = GLES20.glGetUniformLocation(mProgramHandle,
                "u_Texture0");
        mTextureUniformHandle1 = GLES20.glGetUniformLocation(mProgramHandle,
                "u_Texture1");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle,
                "a_Position");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle,
                "a_TexCoordinate");

        /**
         * First texture map
         */
        // 활성 텍스처 0 단위를 텍스처 단위 0으로 설정합니다.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // 이 유닛에 텍스처를 바인드합니다.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle0);

        // texture unit 0에 바인딩하여 셰이더에서 이 텍스처를 사용하도록 텍스처 균일 샘플러에 지시합니다.
        GLES20.glUniform1i(mTextureUniformHandle0, 0);

        /**
         * Second texture map
         */
        // 활성 텍스처 1 단위를 텍스처 단위 1로 설정합니다.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);

        // 이 유닛에 텍스처를 바인드합니다.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle1);

        // 텍스처 유니폼 1에 바인딩하여 셰이더에서
        // 이 텍스처를 사용하도록 텍스처 유니폼 샘플러에 지시합니다.
        GLES20.glUniform1i(mTextureUniformHandle1, 1);

        // 일부 큐브를 그립니다.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -3.2f);
        Matrix.rotateM(mModelMatrix, 0, 0.0f, 1.0f, 1.0f, 0.0f);
        drawCube();
        returebitmap = createBitmapFromGLSurface(0,0,width_surface,height_surface,glUnused);
}

    /**
     * Draws a cube.
     */

    public static Bitmap returebm(){
        return returebitmap;
    }
    private void drawCube() {
        // 위치 정보 전달
        mCubePositions.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize,
                GLES20.GL_FLOAT, false, 0, mCubePositions);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // 텍스처 좌표 정보를 전달합니다.
        mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle,
                mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0,
                mCubeTextureCoordinates);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        // 뷰 행렬에 모델 행렬을 곱하고 결과를 MVP 행렬 (현재 모델 * 뷰 포함)에 저장합니다.
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // 그러면 모델 뷰 행렬에 투영 행렬이 곱해지고 결과가 MVP 행렬에 저장됩니다 (이제는 모델 * 뷰 * 투영이 포함됨).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        // 결합 된 행렬을 전달하십시오.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // 큐브를 그립니다.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
    }

    private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h, GL10 gl)
            throws OutOfMemoryError {
        int bitmapBuffer[] = new int[w * h];
        int bitmapSource[] = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);

        try {
            gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
            int offset1, offset2;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    int texturePixel = bitmapBuffer[offset1 + j];
                    int blue = (texturePixel >> 16) & 0xff;
                    int red = (texturePixel << 16) & 0x00ff0000;
                    int pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }
        } catch (GLException e) {
            return null;
        }

        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
    }
}