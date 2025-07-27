# **RreadMe**

## app简介

一个与网易云音乐类似的app，主要实现了音乐相关功能，音乐播放，mv播放，搜索等等

## 本人负责的主要模块

我主要负责login模块（由于接口后续用不了了，没办法展示了🥲），personage模块，hot模块，search模块，以及mv播放模块

#### login模块

登录后传递用户信息使用了拦截器自动为后续请求加上cookie，SharedPreferences用来保存

#### personage模块

使用 `AlertDialog.Builder` 创建选择对话框（“拍照 / 相册选择 / 取消”），通过 `setItems` 设置选项列表，通过 `Intent(MediaStore.ACTION_IMAGE_CAPTURE)` 启动系统相机，使用 `MediaStore.EXTRA_OUTPUT` 指定拍照结果的存储路径，实现拍照功能，通过 `AvatarStorageUtil` 工具类，将头像 `Uri` 转为字符串后存储到 `SharedPreferences`

[Music/xt的README/2d018faf5f9070a0182ee6ef8e39565e.gif at main · Masumany/Music](https://github.com/Masumany/Music/blob/main/xt的README/2d018faf5f9070a0182ee6ef8e39565e.gif)

#### Hot模块

主要是rv和网络请求，导航用的BottomNavigationView，和vp2联动，用SwipeRefreshLayout实现了下拉刷新的效果，listFragment使用了两次网络请求，加载的慢，即使使用了缓存还是效果不佳

[Music/xt的README/4fc35c681a5db7ba4cf67f3645484575.gif at main · Masumany/Music](https://github.com/Masumany/Music/blob/main/xt的README/4fc35c681a5db7ba4cf67f3645484575.gif)

#### search模块

通过 `TextWatcher` 监听输入框文本变化，在 `onTextChanged` 中实时调用 `loadSuggestion(keyWord)`，输入过程中自动加载并显示匹配的搜索建议，搜索结果界面接收SearchActivity传入的 keywords 并显示在输入框，对搜索按钮，搜索框添加监听，通过 `setResult(RESULT_OK, intent)` 将修改后的关键词回传，当 Activity 创建 Fragment 时，通过 `newInstance` （接口）传递关键词

[Music/xt的README/6d5d58ce945f34a4eab6eb07aeae604c.gif at main · Masumany/Music](https://github.com/Masumany/Music/blob/main/xt的README/6d5d58ce945f34a4eab6eb07aeae604c.gif)

#### MV模块

用了`ExoPlayer`（Media3 库的）实现视频播放，实现了MV的分享功能，查看评论使用的是底部弹窗BottomSheet

[Music/xt的README/ca7576ca9e68f241a13a8f2871abd312.gif at main · Masumany/Music](https://github.com/Masumany/Music/blob/main/xt的README/ca7576ca9e68f241a13a8f2871abd312.gif)

### 技术体现

* MVVM架构，实现数据和UI解耦

* 协程 + Flow + ViewModel监听数据
* SharedPreferences储存
* Retrofit，Gson，和协程实现网络请求
* `ExoPlayer`实现视频播放

### 个人感悟

这次考核还是学到很多东西的，实战协程 + Flow，让我对它们的运用熟练了一些，还有ExoPlayer的使用，SwipeRefreshLayout以及BottomSheet等等，是一次很宝贵的经历，多模块开发确实很不容易啊😭（没有说个人开发容易的意思🥲），冲突真的让人头大，不可否认的是，这次经历让我解决问题的能力和思维长进了一些🤏🏻，也很感谢我的队友的帮助和包容，当然我还有很多不足之处，比如：代码结构逻辑欠佳，规范性也欠佳，对一些技术的使用还是不熟练，起名有些不好区分，导致有些地方容易混用🥲，UI的美感......不怎么样（有点丑），想象和现实还是有差距，最后呢，感谢网校提供的平台，让我有机会在这里学习安卓的相关知识，结识一些朋友，周围的人特别的努力好学，学习氛围特别好