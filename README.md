实现

1. 绘制

drawOver

图片点击状态切换
设置ItemTouchListener
拦截用户点击事件
保存按下状态
drawable.setState
invaliate recyclerView

文字的绘制
参考TextView使用staticLayout绘制

2. 滚动
根据百分比
RecyclerView.scrollTo

知识点
1. 如何在非view中使用style、默认style、自定义style参数、获取style指定资源
2. 如何在canvas中“正确”绘制文字（正确的测量，居中，对字体样式的支持）
3. 触摸事件处理
4. 脱离view框架的layoutparam获取、拦截、修改，测量绘制
