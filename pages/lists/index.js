// pages/lists/index.js

const app = getApp();

Page({

  /**
   * 页面的初始数据
   */
  data: {
    speechLists: [],
    searchKeyWord: '',
    selected: []
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    this.loadSpeechLists()
    this.loadSelected()
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
  },
  loadSpeechLists() {
    wx.request({
      url: 'http://localhost:23080/speechlists',
      success: (res) => {
        this.setData({
          speechLists: res.data
        })
      }
    })
  },
  loadSelected() {
    wx.request({
      url: 'http://localhost:23080/selected',
      method: "POST",
      data: {
        'openid': app.globalData.openId
      },
      success: (res) => {
        this.setData({
          selected: JSON.parse(res.data[0].selected)
        })
      }
    })
  },
  buttonTabAdd(e) {
    wx.request({
      url: 'http://localhost:23080/submit',
      method: "POST",
      data: {
        'flag': 1,
        'id': e.currentTarget.dataset.id,
        'openid': app.globalData.openId
      },
      success: () => {
        this.setData({
          selected: this.push(this.data.selected, e.currentTarget.dataset.id)
        })
      }
    })
  },
  buttonTabCancel(e) {
    wx.request({
      url: 'http://localhost:23080/submit',
      method: "POST",
      data: {
        'flag': -1,
        'id': e.currentTarget.dataset.id,
        'openid': app.globalData.openId
      },
      success: () => {
        this.setData({
          selected: this.erase(this.data.selected, e.currentTarget.dataset.id)
        })
      }
    })
  },
  push(e, id) {
    e.push(id.toString())
    return e
  },
  erase(e, id) {
    e.splice(e.indexOf(id.toString()), 1)
    return e
  }
})