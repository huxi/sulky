package de.huxhorn.sulky.codec.filebuffer;

import de.huxhorn.sulky.codec.DelegatingCodecBase;

public class MetaDataCodec
	extends DelegatingCodecBase<MetaData>
{
	public MetaDataCodec()
	{
		super(new MetaDataEncoder(true), new MetaDataDecoder(true));
	}
}
