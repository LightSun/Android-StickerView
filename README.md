# Android-StickerView
* the sticker view which support scale and drag. often used for mark bitmap to another. eg : pdf
* 用途： 可用于pdf签字的操作控件
* ![demo1](https://github.com/LightSun/Android-StickerView/blob/master/imgs/sticker_view.gif)


# Install
- Gradle
```java
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

 implementation 'com.github.LightSun:Android-StickerView:<see release>'
```
## 自定义属性详解
```java

         <!-- sticker初始化宽高 -->
        <attr name="stv_sticker_init_width" format="dimension|reference"/>
        <attr name="stv_sticker_init_height" format="dimension|reference"/>
	
        <!-- sticker初始化比例，根据设置的bitmap来-->
        <attr name="stv_sticker_init_scale_ratio" format="float|reference"/>
        <!-- 虚线的颜色 -->
        <attr name="stv_line_color" format="color|reference"/>
	<!-- 虚线的pathEffect 参数
        <attr name="stv_line_pe_interval" format="float|reference"/>
	<!-- 虚线的pathEffect 参数
        <attr name="stv_line_pe_phase" format="float|reference"/>
        
        <!-- 4个圆角点的半径 和颜色-->
        <attr name="stv_dotRadius" format="dimension|reference"/>
        <attr name="stv_dotColor" format="color|reference"/>

        <!-- 是否渲染文本在sticker右边 -->
        <attr name="stv_text_enable" format="boolean|reference"/>
	<!-- 文本 -->
        <attr name="stv_text" format="string|reference"/>
	<!-- 文本背景圆角 -->
        <attr name="stv_text_bg_round" format="dimension|reference"/>
	<!-- 文本背景颜色 -->
        <attr name="stv_text_bg_color" format="color|reference"/>
	<!-- 文本颜色 -->
        <attr name="stv_text_color" format="color|reference"/>
	<!-- 文本大小 -->
        <attr name="stv_text_size" format="dimension|reference"/>
	<!-- 距离sticker的间距 -->
        <attr name="stv_text_marginStart" format="dimension|reference"/>
        <attr name="stv_text_padding_start" format="dimension|reference"/>
        <attr name="stv_text_padding_top" format="dimension|reference"/>
        <attr name="stv_text_padding_bottom" format="dimension|reference"/>
        <attr name="stv_text_padding_end" format="dimension|reference"/>

        <!-- 是否等比缩放 -->
        <attr name="stv_proportional_zoom" format="boolean|reference"/>
        <!-- 内容距离start 比如-100. 表示距离右边100 -->
        <attr name="stv_content_margin_start" format="dimension|reference"/>
	<!-- 内容距离top 比如-100. 表示距离底部100 -->
        <attr name="stv_content_margin_top" format="dimension|reference"/>

        <!-- 最小缩放和最大缩放-->
        <attr name="stv_min_scale" format="float|reference"/>
        <attr name="stv_max_scale" format="float|reference"/>
        <!-- touch padding as slop 触发拖拽的slop值。 -->
        <attr name="stv_touch_padding" format="dimension|reference"/>
```


 ## License

    Copyright 2019  
                    heaven7(donshine723@gmail.com)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
