package upt.ssc.bf.core.model

import org.apache.commons.codec.digest.Crypt

case class Contract(password: String) {
  def isEmperor(implicit intel: EmperorIntel): Boolean =
    Crypt.crypt(password, intel.salt) == intel.hash

}
