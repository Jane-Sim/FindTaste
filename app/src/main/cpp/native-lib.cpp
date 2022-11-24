#include <jni.h>
#include <string>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/opencv.hpp>
#include <android/log.h>

using namespace cv;
using namespace std;

double alpha = 1.0; /*< Simple contrast control */
int beta=30;  /*< Simple brightness control */
bool faceYN = false;
bool eyesYN = false;

/*extern "C"
JNIEXPORT void JNICALL
Java_com_example_seyoung_opencv_MainActivity_ConvertRGBtoGray(JNIEnv *env, jobject instance,
                                                              jlong matAddrInput,
                                                              jlong matAddrResult) {
    Mat &matInput = *(Mat *)matAddrInput;
    Mat &matResult = *(Mat *)matAddrResult;

    cvtColor(matInput, matResult, CV_RGBA2GRAY);
    cvtColor(matInput, matResult, CV_RGB);

    // TODO

}*/
extern "C" {
JNIEXPORT jlong JNICALL
Java_com_example_seyoung_findtaste_views_MainFragment_opencv_loadCascade(JNIEnv *env, jobject instance,
                                                         jstring cascadeFileName_) {
    const char *nativeFileNameString = env->GetStringUTFChars(cascadeFileName_, 0);

    string baseDir("/storage/emulated/0/");
    baseDir.append(nativeFileNameString);
    const char *pathDir = baseDir.c_str();

    jlong ret = 0;
    ret = (jlong) new CascadeClassifier(pathDir);
    if (((CascadeClassifier *) ret)->empty()) {
        __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                            "CascadeClassifier로 로딩 실패  %s", nativeFileNameString);
    }
    else
        __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                            "CascadeClassifier로 로딩 성공 %s", nativeFileNameString);

    env->ReleaseStringUTFChars(cascadeFileName_, nativeFileNameString);
    return ret;
}

float resize(Mat img_src, Mat &img_resize, int resize_width) {

    float scale = resize_width / (float) img_src.cols;
    if (img_src.cols > resize_width) {
        int new_height = cvRound(img_src.rows * scale);
        resize(img_src, img_resize, Size(resize_width, new_height));
    } else {
        img_resize = img_src;
    }
    return scale;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_seyoung_findtaste_views_MainFragment_opencv_detect(JNIEnv *env, jobject instance,
                                                    jlong cascadeClassifier_face,
                                                    jlong cascadeClassifier_eye,
                                                    jlong matAddrInput,
                                                    jlong matAddrResult) {

    // TODO
    Mat &img_input = *(Mat *) matAddrInput;
    Mat &img_result = *(Mat *) matAddrResult;

    img_result = img_input.clone();

    std::vector<Rect> faces;
    Mat img_gray;

    cvtColor(img_input, img_gray, COLOR_BGR2GRAY);
    equalizeHist(img_gray, img_gray);

    Mat img_resize;
    float resizeRatio = resize(img_gray, img_resize, 640);

    //-- Detect faces
    ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.1, 5,
                                                                     0 | CASCADE_SCALE_IMAGE,
                                                                     Size(30, 30));


    __android_log_print(ANDROID_LOG_DEBUG, (char *) "native-lib :: ",
                        (char *) "face %d found ", (int) faces.size());

    // 얼굴을 1개 이상 인식할 때
    for (int i = 0; i < faces.size(); i++) {
        double real_facesize_x = faces[i].x / resizeRatio;
        double real_facesize_y = faces[i].y / resizeRatio;
        double real_facesize_width = faces[i].width / resizeRatio;
        double real_facesize_height = faces[i].height / resizeRatio;

        Point center( real_facesize_x + real_facesize_width / 2, real_facesize_y + real_facesize_height/2);
        ellipse(img_result, center, Size( real_facesize_width / 2, real_facesize_height / 2), 0, 0, 360,
                Scalar(255, 0, 255), 30, 8, 0);


        Rect face_area(real_facesize_x, real_facesize_y, real_facesize_width, real_facesize_height);
        Mat faceROI = img_gray(face_area);
        std::vector<Rect> eyes;

        //-- In each face, detect eyes
        ((CascadeClassifier *) cascadeClassifier_eye)->detectMultiScale(faceROI, eyes, 1.1, 3,
                                                                        0 | CASCADE_SCALE_IMAGE,
                                                                        Size(30, 30));
        //눈을 1개 이상 인식할 때.
        for (size_t j = 0; j < eyes.size(); j++) {
            Point eye_center(real_facesize_x + eyes[j].x + eyes[j].width / 2,
                             real_facesize_y + eyes[j].y + eyes[j].height / 2);
            int radius = cvRound((eyes[j].width + eyes[j].height) * 0.25);
            circle(img_result, eye_center, radius, Scalar(255, 0, 0), 30, 8, 0);
        }
        if(eyes.size()>1)
            eyesYN= true;

    }
    if(faces.size()<1)
    faceYN = false;
    else
    faceYN = true;
}



}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_seyoung_findtaste_views_MainFragment_opencv_imwrite(JNIEnv *env, jclass type,
                                                                     jstring filename_,
                                                                     jobject img) {
    if(faceYN) {
        const char *filename = env->GetStringUTFChars(filename_, 0);

        // TODO

        env->ReleaseStringUTFChars(filename_, filename);
    }
}


extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_seyoung_findtaste_views_MainFragment_opencv_loadbool(JNIEnv *env, jclass type) {
    bool picYN =false;
    if(faceYN)
        picYN= true;

    // TODO
    return (jboolean) picYN;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_seyoung_findtaste_views_ActivityFilter_loadImage(
        JNIEnv *env,
        jobject,
        jstring imageFileName,
        jlong addrImage) {

    Mat &img_input = *(Mat *) addrImage;

    const char *nativeFileNameString = env->GetStringUTFChars(imageFileName, JNI_FALSE);

    string baseDir("");
    baseDir.append(nativeFileNameString);
    const char *pathDir = baseDir.c_str();

    img_input = imread(pathDir, IMREAD_COLOR);

}

/*
extern "C"
JNIEXPORT void JNICALL
Java_com_example_seyoung_findtaste_views_ActivityFilter_imageprocessing(
        JNIEnv *env,
        jobject,
        jlong addrInputImage) {

    Mat &img_input = *(Mat *) addrInputImage;
    cvtColor( img_input, img_input, CV_BGR2RGB);
    //사진의 원래 색깔로 바꿔준다.
    //  Mat &img_output = *(Mat *) addrOutputImage;
    Mat srcImage = img_input;

    int border = 3;
    Size ksize (border*2+1,border*2+1);
    Mat destImage2;
    int d = ksize.width;
    double sigmaColor =50.0;
    double sigmaSpace = 50.0;
    //경계선을 보존하면서, 동시에 경계가 아닌 것(경계가 아니므로 smooth region으로 표현)은 노이즈를 제거할까? 이러한 것을 하기 위해 많은 노력이 있었다.  Bilateral filtering 은 단순하면서, 반복적이지 않은(non-iterative) 방법이다.
    bilateralFilter(srcImage,destImage2,10,75,75);
    destImage2.copyTo(img_input);
    //
    Mat new_image = Mat::zeros( img_input.size(), img_input.type() );

    for( int y = 0; y < img_input.rows; y++ ) {
        for( int x = 0; x < img_input.cols; x++ ) {
            for( int c = 0; c < 3; c++ ) {
                new_image.at<Vec3b>(y,x)[c] =
                        // 밝기와 선명함
                        saturate_cast<uchar>( alpha*( img_input.at<Vec3b>(y,x)[c] ) + beta );

                //saturate_cast<uchar>( img_input.at<Vec3b>(y,x)[c] +100 );

            }
        }
    }

    new_image.copyTo(img_input);


    */
/*   vector<Mat> channels;
       Mat img_hist_equalized;
       //change the color image from BGR to YCrCb format
       cvtColor(img_input, img_hist_equalized, CV_BGR2YCrCb);
       //split the image into channels
       split(img_hist_equalized,channels);
       //equalize histogram on the 1st channel (Y)
       equalizeHist(channels[0], channels[0]);
       //merge 3 channels including the modified 1st channel into one image
       merge(channels,img_hist_equalized);
       //change the color image from YCrCb to BGR format (to display image properly)
       cvtColor(img_hist_equalized, img_hist_equalized, CV_YCrCb2BGR);

       img_hist_equalized.copyTo(img_input);*//*

    //cvtColor( img_input, img_input, CV_BGR2HSV);
    */
/*cvtColor( img_input, img_input, CV_RGB2GRAY);
    blur( img_input, img_input, Size(5,5) );
    Canny( img_input, img_input, 50, 150, 5 );*//*

}
*/

extern "C"
JNIEXPORT void JNICALL
Java_com_example_seyoung_findtaste_views_ActivityFilter_imageprocessing(
        JNIEnv *env,
        jobject,
        jlong addrInputImage,
        jint page) {
    Mat &img_input = *(Mat *) addrInputImage;
    cvtColor(img_input, img_input, CV_BGR2RGB);
    //사진의 원래 색깔로 바꿔준다.
    //  Mat &img_output = *(Mat *) addrOutputImage;
    if (page == 2) {

    Mat destImage2;

    //경계선을 보존하면서, 동시에 경계가 아닌 것(경계가 아니므로 smooth region으로 표현)은 노이즈를 제거할까? 이러한 것을 하기 위해 많은 노력이 있었다.  Bilateral filtering 은 단순하면서, 반복적이지 않은(non-iterative) 방법이다.
    bilateralFilter(img_input, destImage2, 10, 75, 75);
   // destImage2.copyTo(img_input);
    //
    Mat new_image = Mat::zeros(destImage2.size(), destImage2.type());

    for (int y = 0; y < destImage2.rows; y++) {
        for (int x = 0; x < destImage2.cols; x++) {
            for (int c = 0; c < 3; c++) {
                new_image.at<Vec3b>(y, x)[c] =
                        // 밝기와 선명함
                        saturate_cast<uchar>(alpha * (destImage2.at<Vec3b>(y, x)[c]) + beta);

                //saturate_cast<uchar>( img_input.at<Vec3b>(y,x)[c] +100 );

            }
        }
    }
   // img_input.convertTo(new_image,-1,alpha,beta);
    new_image.copyTo(img_input);

}
}
    /*   vector<Mat> channels;
       Mat img_hist_equalized;
       //change the color image from BGR to YCrCb format
       cvtColor(img_input, img_hist_equalized, CV_BGR2YCrCb);
       //split the image into channels
       split(img_hist_equalized,channels);
       //equalize histogram on the 1st channel (Y)
       equalizeHist(channels[0], channels[0]);
       //merge 3 channels including the modified 1st channel into one image
       merge(channels,img_hist_equalized);
       //change the color image from YCrCb to BGR format (to display image properly)
       cvtColor(img_hist_equalized, img_hist_equalized, CV_YCrCb2BGR);

       img_hist_equalized.copyTo(img_input);*/
    //cvtColor( img_input, img_input, CV_BGR2HSV);
    /*cvtColor( img_input, img_input, CV_RGB2GRAY);
    blur( img_input, img_input, Size(5,5) );
    Canny( img_input, img_input, 50, 150, 5 );*/
    // TOD