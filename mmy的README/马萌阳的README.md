## 马萌阳的Readme

### APP简介


这是由马萌阳和徐彤一起开发的一款音乐播放APP。我负责的模块是`app`模块，`lib_base`模块，`module_detail`模块，`module_musicplayer`模块，以及`module_recommend`模块。  

### module介绍



#### app模块

这个模块我使用了Drawerlayout+NavigationView实现抽屉式布局，运用底部导航栏和vp2实现不同页面的切换。音乐播放底部栏与module_musicplayer模块的MusicPlayerService绑定，运用LiveData观察歌曲的变化能够及时更新UI和底部栏图片旋转状态。底部栏中的歌单显示我使用了bottomsheetdialog。刚进入app时的动画也是写在了这个模块，运用缩放和淡化的动画，比较美观。
![](https://github.com/Masumany/Music/blob/main/mmy%E7%9A%84README/fe596c5b3dfa4d3b25598c07b2e6bd14.gif)
![](https://github.com/Masumany/Music/blob/main/mmy%E7%9A%84README/aa5dc52d893f99fd9e2d627a60fb695f.gif)


#### lib_base模块

这个模块主要是对我在其他模块要用的数据类，api等的封装。在前期写的时候没有对位于不同板块歌曲的数据类有深入的研究，以为都是一样的。写到后面发现我写的歌曲的数据类不同，将不同数据类的转换也写在了这个里面，便于在播放器中统一使用。

#### module_detail模块

这个模块主要是对音乐播放器里面各个详细的板块的深入。

1.点击音乐播放器页面中的歌手名字，会跳转到这个模块。包括歌手的百科，相似的艺人，歌手的最热歌曲以及MV。在歌手的最热歌曲中，运用PopUpWindow展示分享与下载。  

![](https://github.com/Masumany/Music/blob/main/mmy%E7%9A%84README/3e4414c83e272ae69000a3ad2ff0228b.gif)

2.点击音乐播放器的封面会跳转到这个模块的歌词界面。实现歌词会随音乐的播放而滚动，为了实现这个效果，我重新定义歌词的数据类，运用正则表达式将时间戳与歌词分隔开。同时，使用了EventBus与播放器进行通信，LiveData观察，及时更新并标记当前播放的歌词，在自动切换下一首歌时，也能及时关闭。在写歌词滚动的时候也出现了许多问题，歌词很多时候就不自动滚动了或者退出这个界面再进来，当前的歌词也不变了...改了也是有些时间的。  

![](https://github.com/Masumany/Music/blob/main/mmy%E7%9A%84README/25e031327ff440959d176283e338e22e.gif)

#### module_musicplayer模块

这个模块主要是音乐的播放，这个模块我用的时间和出的问题是最多的。前期写的时候在虚拟机上跑，播放，暂停，切换歌曲都是正常的，后面用真机调试的时候发现seekbar也拖不动，暂停，播放也不管用，也是被虚拟机坑了...在这个模块，我主要写了以下内容：

1.音乐播放功能，获取传入的音乐id，再用api转为url，最后用MediaPlayer实现音乐的播放。

2.暂停，播放；下一首，上一首；随机播放，顺序播放以及单曲循环；音乐列表。可以实现连播的效果，与首页的音乐底部栏进行联动。音乐列表获取当前列表，并将正在播放的音乐显示为高亮状态。  

![](https://github.com/Masumany/Music/blob/main/mmy%E7%9A%84README/f0fd2ea7ce0a36bdd5270407bb4ff04c.gif)  
![](https://github.com/Masumany/Music/blob/main/mmy%E7%9A%84README/fd1aa33e35c219fcbcf1571f3f39a05b.gif)  
3.收藏；评论；分享。收藏状态使用sharedPreferences进行保存，评论比较简单就是一个Fragment。
![](https://github.com/Masumany/Music/blob/main/mmy%E7%9A%84README/08e08a8bba0fb66e71a1f01909c9727d.gif)  
4.动画。我写了个动画，一个是歌曲封面的旋转；另一个是仿网易云的固定角度的播放杆的旋转。与播放状态联动，播放暂停动画会停止或者旋转到初始位置。

#### module_recommend模块

这个模块主要是写在主页的内容，包括下拉刷新，Banner，每日推荐歌单和歌曲。

1.Banner，运用vp2和Handle的定时任务实现自动轮播。

2.每日推荐歌单这个板块里，我写了一个动画，向上滑动时自动隐藏专辑区，更加便捷。顶部的播放全部默认从索引为0的歌曲开始播放。由于这些歌曲列表过大，我写了个缓存列表，这样就不会因为给音乐播放器传递过大的列表而报错。  
![](https://github.com/Masumany/Music/blob/main/mmy%E7%9A%84README/3c202506cade05848bde453c386aae7a.gif)  
3.歌曲板块，我使用的是rv和分页逻辑。

### 技术亮点


* 使用MVVM框架
* 使用了Rxjava+Retrofit进行网络请求，在新的线程里请求，主线程接收
* activity和fragment之间的通信用viewmodel，用对外开放的Livedata进行UI的更新
* 使用TheRouter进行模块间的跳转和传参
* 使用handle刷新UI
* 使用Service进行后台播放
* 使用EventBus进行通信

### 个人感悟

在这一个月的学习中，我学习到了很多新的知识。在写课件的时候刚学习完多模块化，就用到了暑假考核里。包括在写考核的时候也学习到了很多的新知识并及时运用，不断地增进自己的技术。回顾在网校学习的一年，从刚开始的登录界面到现在的完整的APP，这一路上我学到了很多的知识，大一这一年可以说过的很充实。Android开发随着不断地学习，逐渐拓宽了我的眼界。但是还有许多自己还没来得及深入了解和使用的知识，比如事件的分发，滑动冲突，自定义view...自己真正掌握的知识其实还是有点片面的。感谢网校提供给我学习的平台，让我有机会在良好的学习氛围中不断努力，进步。
