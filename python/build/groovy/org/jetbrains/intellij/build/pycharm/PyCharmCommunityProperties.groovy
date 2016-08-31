/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.intellij.build.pycharm

import org.jetbrains.intellij.build.*

/**
 * @author nik
 */
class PyCharmCommunityProperties extends PyCharmPropertiesBase {
  PyCharmCommunityProperties(String home) {
    super(home)
    productCode = "PC"
    platformPrefix = "PyCharmCore"
    applicationInfoModule = "python-community-ide-resources"
    brandingResourcePaths = ["$home/community/python/resources"]

    productLayout.platformApiModules = CommunityRepositoryModules.PLATFORM_API_MODULES + ["dom-openapi"]
    productLayout.platformImplementationModules = CommunityRepositoryModules.PLATFORM_IMPLEMENTATION_MODULES + [
      "dom-impl", "python-community", "python-community-ide-resources",
      "python-ide-community", "python-community-configure", "python-openapi", "python-psi-api", "platform-main"
    ]
    productLayout.bundledPluginModules = new File("$home/community/python/build/plugin-list.txt").readLines()
    productLayout.mainModule = "main_pycharm_ce"
  }

  @Override
  void copyAdditionalFiles(BuildContext context, String targetDirectory) {
    super.copyAdditionalFiles(context, targetDirectory)
    context.ant.copy(todir: "$targetDirectory/license") {
      fileset(file: "$context.paths.communityHome/LICENSE.txt")
      fileset(file: "$context.paths.communityHome/NOTICE.txt")
    }
  }

  @Override
  String systemSelector(ApplicationInfoProperties applicationInfo) {
    "PyCharmCE${applicationInfo.majorVersion}.${applicationInfo.minorVersionMainPart}"
  }

  @Override
  String baseArtifactName(ApplicationInfoProperties applicationInfo, String buildNumber) {
    "pycharmPC-$buildNumber"
  }

  @Override
  WindowsDistributionCustomizer createWindowsCustomizer(String projectHome) {
    return new PyCharmWindowsDistributionCustomizer() {
      {
        buildZipWithBundledOracleJre = true
        installerImagesPath = "$projectHome/community/python/build/resources"
        fileAssociations = [".py"]
      }

      @Override
      String fullNameIncludingEdition(ApplicationInfoProperties applicationInfo) {
        "PyCharm Community Edition"
      }

      @Override
      void copyAdditionalFiles(BuildContext context, String targetDirectory) {
        super.copyAdditionalFiles(context, targetDirectory)
        context.ant.copy(file: "$context.paths.projectHome/python/help/pycharmhelp.jar", todir: "$targetDirectory/help", failonerror: false)
      }
    }
  }

  @Override
  LinuxDistributionCustomizer createLinuxCustomizer(String projectHome) {
    return new LinuxDistributionCustomizer() {
      {
        iconPngPath = "$projectHome/community/python/resources/PyCharmCore128.png"
      }
      @Override
      String rootDirectoryName(ApplicationInfoProperties applicationInfo, String buildNumber) {
        "pycharm-community-${applicationInfo.isEAP ? buildNumber : applicationInfo.fullVersion}"
      }

      @Override
      void copyAdditionalFiles(BuildContext context, String targetDirectory) {
        context.ant.copy(file: "$context.paths.projectHome/python/help/pycharmhelp.jar", todir: "$targetDirectory/help", failonerror: false)
      }
    }
  }

  @Override
  MacDistributionCustomizer createMacCustomizer(String projectHome) {
    return new PyCharmMacDistributionCustomizer() {
      {
        icnsPath = "$projectHome/community/python/resources/PyCharmCore.icns"
        bundleIdentifier = "com.jetbrains.pycharm"
        helpId = "PY"
        dmgImagePath = "$projectHome/community/python/build/DMG_background.png"
      }

      @Override
      String rootDirectoryName(ApplicationInfoProperties applicationInfo, String buildNumber) {
        String suffix = applicationInfo.isEAP ? " ${applicationInfo.majorVersion}.${applicationInfo.minorVersion} EAP" : ""
        "PyCharm CE${suffix}.app"
      }
    }
  }
}