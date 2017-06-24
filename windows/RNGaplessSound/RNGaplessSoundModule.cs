using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace Com.Reactlibrary.RNGaplessSound
{
    /// <summary>
    /// A module that allows JS to share data.
    /// </summary>
    class RNGaplessSoundModule : NativeModuleBase
    {
        /// <summary>
        /// Instantiates the <see cref="RNGaplessSoundModule"/>.
        /// </summary>
        internal RNGaplessSoundModule()
        {

        }

        /// <summary>
        /// The name of the native module.
        /// </summary>
        public override string Name
        {
            get
            {
                return "RNGaplessSound";
            }
        }
    }
}
