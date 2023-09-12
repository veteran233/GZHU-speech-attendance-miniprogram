// pages/lists/index.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    speechLists: [
      { id: 1, name: '导弹设计与开发', type: '科技类', location: '理科南110', startTime: '13:50', duration: 45, state: '空余' },
      { id: 2, name: '数字化与中国企业', type: '企业类', location: '理科南110', startTime: '13:50', duration: 45, state: '空余' },
      { id: 3, name: '品牌战略与企业增长', type: '企业类', location: '理科南110', startTime: '13:50', duration: 45, state: '已满' }
    ],
    searchKeyWord: '',
    matchKeyWord: true
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {

  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  },

  getSearchBoxData(e) {
    this.setData({
      searchKeyWord: e.detail.value
    })
  }
})