// index.js

const app = getApp()

Page({
  data: {},
  // 事件处理函数
  bindViewTap() {
    wx.navigateTo({
      url: '../logs/logs'
    })
  },
  onLoad() {
    this.login()
  },
  login(e) {
    wx.login({
      success: (res) => {
        wx.request({
          url: 'http://localhost:23080/login',
          method: 'POST',
          data: res.code,
          success: (res) => {
            if (res.data.reg == true) {
              app.globalData.hasOpenId = true
              app.globalData.openId = res.data.openid
              this.toLists()
            }
            else {
              wx.navigateTo({
                url: '../reg/index'
              })
            }
          }
        })
      }
    })
  },
  toLists() {
    wx.navigateTo({
      url: '../lists/index'
    })
  }
})
