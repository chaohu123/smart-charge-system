import SockJS from 'sockjs-client'
import { Stomp } from 'stompjs'

class WebSocketClient {
  constructor() {
    this.stompClient = null
    this.connected = false
  }

  connect(token, onConnect, onError) {
    const socket = new SockJS('/ws')
    this.stompClient = Stomp.over(socket)
    
    this.stompClient.connect(
      { Authorization: `Bearer ${token}` },
      (frame) => {
        this.connected = true
        if (onConnect) onConnect(frame)
      },
      (error) => {
        this.connected = false
        if (onError) onError(error)
      }
    )
  }

  subscribe(destination, callback) {
    if (this.stompClient && this.connected) {
      return this.stompClient.subscribe(destination, (message) => {
        const data = JSON.parse(message.body)
        if (callback) callback(data)
      })
    }
    return null
  }

  send(destination, data) {
    if (this.stompClient && this.connected) {
      this.stompClient.send(destination, {}, JSON.stringify(data))
    }
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.disconnect()
      this.connected = false
    }
  }
}

export default new WebSocketClient()


