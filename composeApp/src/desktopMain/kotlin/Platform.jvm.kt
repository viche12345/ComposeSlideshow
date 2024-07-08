import io.github.vinceglb.filekit.core.PlatformFile

actual fun PlatformFile.absolutePath(): String? = file.absolutePath
