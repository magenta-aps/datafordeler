import React from "react"

// Create the layout component
class OperationsLayout extends React.Component {
  render() {
    const {
      getComponent
    } = this.props

    const Operations = getComponent("operations", true)

    return React.createElement("div", null, React.createElement(Operations, null));
    
  }
}

// Create the plugin that provides our layout component
const OperationsLayoutPlugin = () => {
  return {
    components: {
      OperationsLayout: OperationsLayout
    }
  }
}
