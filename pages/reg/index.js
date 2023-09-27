// pages/reg/index.js

const app = getApp()

Page({
  data: {
    studentName: '',
    studentId: ''
  },
  onLoad(options) {

  },
  onReady() {

  },
  onShow() {

  },
  onHide() {

  },
  onUnload() {

  },
  onPullDownRefresh() {

  },
  onReachBottom() {

  },
  onShareAppMessage() {

  },
  getStudentName(e) {
    this.setData({
      studentName: e.detail.value
    })
  },
  getStudentId(e) {
    this.setData({
      studentId: e.detail.value
    })
  },
  buttonTab() {
    wx.login({
      success: (res) => {
        wx.request({
          url: 'http://localhost:23080/register',
          method: "POST",
          data: {
            'js_code': res.code,
            'studentName': this.data.studentName,
            'studentId': this.data.studentId
          },
          success: (res) => {
            app.globalData.hasOpenId = true
            app.globalData.openId = res.data.openid
            wx.navigateTo({
              url: '../lists/index'
            })
          }
        })
      },
    })
  }
})