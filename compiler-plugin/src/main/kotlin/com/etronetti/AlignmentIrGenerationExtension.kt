package com.etronetti

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

/**
 * The generation extension is used to register the transformer plugin, which is going to modify
 * the IR using the function responsible for the alignment.
 */
class AlignmentIrGenerationExtension: IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        // Function that is responsible to handle the alignment
        val alignOnFunction = collect(moduleFragment).first()
        moduleFragment.transform(AlignmentIrElementTransformer(pluginContext, alignOnFunction), null)
    }
}