
export default {
  data () {
    return {
      yellow: '#ff233c',
      green: '#33CC22'
    }
  },
  created () {

  },
  methods: {
    computeCpuColor (data) {
      if (data.task.vCores > data.queue.vCores) {
        return this.yellow
      } else if (data.task.vCores > data.cluster.vCores) {
        return this.yellow
      } else {
        return this.green
      }
    },
    computeMemoryColor (data) {
      if (data.task.memory > data.queue.memory) {
        return this.yellow
      } else if (data.task.memory > data.cluster.memory) {
        return this.yellow
      } else {
        return this.green
      }
    }
  }
}
