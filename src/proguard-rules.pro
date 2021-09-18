# Add any ProGuard configurations specific to this
# extension here.

-keep public class io.mohamed.repositioningtools.RepositioningTools {
    public *;
 }
-keeppackagenames gnu.kawa**, gnu.expr**

-optimizationpasses 4
-allowaccessmodification
-mergeinterfacesaggressively

-repackageclasses 'io/mohamed/repositioningtools/repack'
-flattenpackagehierarchy
-dontpreverify
