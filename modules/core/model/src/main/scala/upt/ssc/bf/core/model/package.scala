package upt.ssc.bf.core

import fs2.Chunk

package object model {
  type Batch = Chunk[Contract]
}
